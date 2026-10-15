package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.enums.GatewayErrorType;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import br.com.issler.fake_gateway.exceptions.GatewayServiceException;
import br.com.issler.fake_gateway.exceptions.PaymentNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    private final IPaymentRepository paymentRepository;
    private final WebhookService webhookService;
    private final ConcurrentTaskScheduler taskScheduler;
    private final long confirmPaymentDelay;

    private static final String CPF_INVALID = "11111111111";
    private static final String CPF_INTERNAL_ERROR = "22222222222";
    private static final String CPF_RATE_LIMITED = "33333333333";
    private static final String CPF_CARD_REJECTED = "44444444444";
    private static final String CPF_PIX_REJECTED = "55555555555";

    public PaymentService(
            @Value("${payment.confirm-delay-ms}")
            long confirmPaymentDelay,
            IPaymentRepository paymentRepository,
            WebhookService webhookService,
            ConcurrentTaskScheduler taskScheduler
    ){
        this.paymentRepository = paymentRepository;
        this.webhookService = webhookService;
        this.taskScheduler = taskScheduler;
        this.confirmPaymentDelay = confirmPaymentDelay;
    }

    @Transactional
    public PaymentEntity process(CreatePaymentDTO createPaymentDTO) {
        // simulate processing the payment and saving it to the database
        validate(createPaymentDTO);

        boolean isPix = createPaymentDTO.paymentMethod().equals(PaymentMethodType.PIX);
        boolean cardReject = CPF_CARD_REJECTED.equals(createPaymentDTO.payerCpf());
        boolean pixReject = CPF_PIX_REJECTED.equals(createPaymentDTO.payerCpf());
        PaymentEntity payment = paymentRepository.save(
                PaymentEntity.builder()
                    .amount(createPaymentDTO.amount())
                    .payerCpf(createPaymentDTO.payerCpf())
                    .paymentMethod(createPaymentDTO.paymentMethod())
                    .paidAt(isPix || cardReject || pixReject ? null : LocalDateTime.now())
                    .status(initialStatus(isPix, cardReject, pixReject))
                    .build()
        );

        if(isPix){
            UUID paymentId = payment.getId();
            taskScheduler.schedule(() -> confirmPayment(paymentId), Instant.now().plusMillis(confirmPaymentDelay));
        }

        return payment;

    }

    private void validate(CreatePaymentDTO dto){
        switch (dto.payerCpf()){
            case CPF_INVALID -> throw new GatewayServiceException("Invalid CPF (simulated)", GatewayErrorType.INVALID_DOCUMENT);
            case CPF_INTERNAL_ERROR -> throw new GatewayServiceException("Internal error (simulated)", GatewayErrorType.INTERNAL_FAILURE);
            case CPF_RATE_LIMITED -> throw new GatewayServiceException("Rate limited (simulated)", GatewayErrorType.RATE_LIMITED);
            default -> {}
        }
    }

    private PaymentStatusType initialStatus(boolean isPix, boolean cardReject, boolean pixReject){
        if(isPix && !pixReject) return PaymentStatusType.PENDING;
        if(cardReject || pixReject) return PaymentStatusType.REJECTED;
        return PaymentStatusType.ACCEPTED;
    }

    private void confirmPayment(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));



        boolean simulateReject = CPF_PIX_REJECTED.equals(payment.getPayerCpf());
        payment.setStatus(simulateReject ? PaymentStatusType.REJECTED : PaymentStatusType.ACCEPTED);
        if (!simulateReject) {
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);
        webhookService.send(payment);
    }
}
