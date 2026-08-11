package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles(profiles = "test")
class PaymentServiceIntegrationTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().containerThreads(10))
            .build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry){
        registry.add("spring.application.your-api-url", wireMock::baseUrl);
    }

    @AfterEach
    void clean() {
        paymentRepository.deleteAll();
    }

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private IPaymentRepository paymentRepository;

    @Test
    @DisplayName("Should saved payment with PIX as payment method and return-it, and call task scheduler to update-it")
    void process() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/webhooks")).willReturn(ok())
        );

        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .amount(new BigDecimal(100))
                .payerCpf("12345678900")
                .paymentMethod(PaymentMethodType.PIX)
                .build();
        PaymentEntity payment = paymentService.process(dto);
        assertEquals(PaymentStatusType.PENDING, payment.getStatus());

        await()
                .atMost(Duration.FIVE_SECONDS)
                .untilAsserted(() -> {
                    PaymentEntity updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

                    wireMock.verify(postRequestedFor(urlEqualTo("/v1/webhooks")));
                    assertEquals(PaymentStatusType.ACCEPTED, updatedPayment.getStatus());
                });

        assertEquals(1, paymentRepository.count());
    }
}