package br.com.issler.fake_gateway.utils;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.enums.GatewayErrorType;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import br.com.issler.fake_gateway.exceptions.GatewayServiceException;
import br.com.issler.fake_gateway.exceptions.PaymentNotFoundException;
import br.com.issler.fake_gateway.services.WebhookService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentProcessor {
    private final IPaymentRepository paymentRepository;
    private final WebhookService webhookService;
    private final ConcurrentTaskScheduler taskScheduler;
    private final long confirmPaymentDelay;

    private static final String CPF_INVALID = "11111111111";
    private static final String CPF_INTERNAL_ERROR = "22222222222";
    private static final String CPF_RATE_LIMITED = "33333333333";
    private static final String CPF_CARD_REJECTED = "44444444444";
    private static final String CPF_PIX_REJECTED = "55555555555";

    public PaymentProcessor(
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
    public PaymentEntity save(CreatePaymentDTO dto){
        validate(dto);

        boolean isPix = dto.paymentMethod().equals(PaymentMethodType.PIX);
        boolean cardReject = CPF_CARD_REJECTED.equals(dto.payerCpf());
        boolean pixReject = CPF_PIX_REJECTED.equals(dto.payerCpf());
        PaymentStatusType initialStatus = initialStatus(isPix, cardReject, pixReject);

        return paymentRepository.save(
                PaymentEntity.builder()
                        .amount(dto.amount())
                        .payerCpf(dto.payerCpf())
                        .paymentMethod(dto.paymentMethod())
                        .paidAt(isPix || cardReject || pixReject ? null : LocalDateTime.now())
                        .status(initialStatus)
                        .build()
        );
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



    public void schedulePaymentConfirmation(UUID paymentId){
        taskScheduler.schedule(() -> confirmPayment(paymentId), Instant.now().plusMillis(confirmPaymentDelay));
    }

    // this method is called by a different thread = auto different transactional, so just marked it as @Transactional for be implicit
    @Transactional
    public void confirmPayment(UUID paymentId) {
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
