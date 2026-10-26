package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IIdempotencyKeyRepository;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.dtos.PaymentResponse;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import br.com.issler.fake_gateway.exceptions.ErrorResponse;
import br.com.issler.fake_gateway.exceptions.GatewayServiceException;
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

import java.math.BigDecimal;
import java.util.UUID;

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
        idempotencyKeyRepository.deleteAll();
    }

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private IPaymentRepository paymentRepository;
    @Autowired
    private IIdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    @DisplayName("Should saved payment with PIX as payment method and return-it, and call task scheduler to update-it")
    void process() throws Exception {
        wireMock.stubFor(
                post(urlEqualTo("/v1/webhooks")).willReturn(ok())
        );

        UUID idempotencyKey = UUID.randomUUID();
        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .amount(new BigDecimal(100))
                .payerCpf("12345678900")
                .paymentMethod(PaymentMethodType.PIX)
                .build();

        GatewayResponse response = paymentService.process(dto, idempotencyKey.toString());
        assertInstanceOf(PaymentResponse.class, response);



        PaymentResponse paymentResponse = (PaymentResponse) response;
        assertEquals(PaymentStatusType.PENDING, paymentResponse.status());

        await()
                .atMost(Duration.FIVE_SECONDS)
                .untilAsserted(() -> {
                    PaymentEntity updatedPayment = paymentRepository.findById(paymentResponse.gatewayPaymentId()).orElseThrow();

                    wireMock.verify(postRequestedFor(urlEqualTo("/v1/webhooks")));
                    assertEquals(PaymentStatusType.ACCEPTED, updatedPayment.getStatus());
                });
    }

    @Test
    @DisplayName("Should return the same response on retry with the same idempotency key")
    void processWithSameKey() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .amount(new BigDecimal(100))
                .payerCpf("12345678900")
                .paymentMethod(PaymentMethodType.CREDIT_CARD)
                .build();

        GatewayResponse first = paymentService.process(dto, idempotencyKey.toString());
        GatewayResponse second = paymentService.process(dto, idempotencyKey.toString());

        assertEquals(first, second);
        assertEquals(1, paymentRepository.count());
        assertEquals(1, idempotencyKeyRepository.count());
    }

    @Test
    @DisplayName("Shouldn't save idempotency key when the document is invalid")
    void processWithInvalidDocument() {
        UUID idempotencyKey = UUID.randomUUID();
        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .amount(new BigDecimal(100))
                .payerCpf("11111111111")
                .paymentMethod(PaymentMethodType.CREDIT_CARD)
                .build();

        assertThrows(GatewayServiceException.class, () -> paymentService.process(dto, idempotencyKey.toString()));

        assertEquals(0, paymentRepository.count());
        assertEquals(0, idempotencyKeyRepository.count());
    }

    @Test
    @DisplayName("Should save idempotency key when internal failure occurs")
    void processWithInternalFailure() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .amount(new BigDecimal(100))
                .payerCpf("22222222222")
                .paymentMethod(PaymentMethodType.CREDIT_CARD)
                .build();

        GatewayResponse response = paymentService.process(dto, idempotencyKey.toString());

        assertInstanceOf(ErrorResponse.class, response);
        assertEquals(0, paymentRepository.count());
        assertEquals(1, idempotencyKeyRepository.count());
    }
}