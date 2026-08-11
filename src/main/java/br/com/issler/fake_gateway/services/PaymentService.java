package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
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
        // always accept cards and set status to PENDING for PIX payments
        PaymentEntity payment = paymentRepository.save(
                PaymentEntity.builder()
                    .amount(createPaymentDTO.amount())
                    .payerCpf(createPaymentDTO.payerCpf())
                    .paymentMethod(createPaymentDTO.paymentMethod())
                    .paidAt(createPaymentDTO.paymentMethod().equals(PaymentMethodType.PIX) ? null : LocalDateTime.now())
                    .status( createPaymentDTO.paymentMethod().equals(PaymentMethodType.PIX) ?
                        PaymentStatusType.PENDING : PaymentStatusType.ACCEPTED)
                    .build()
        );

        if(createPaymentDTO.paymentMethod().equals(PaymentMethodType.PIX)){
            UUID paymentId = payment.getId();
            taskScheduler.schedule(() -> confirmPayment(paymentId), Instant.now().plusMillis(confirmPaymentDelay));
        }

        return payment;

    }

    private void confirmPayment(UUID paymentId) {
        // always accepted in order to simulate just
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatusType.ACCEPTED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        webhookService.send(payment);
    }
}
