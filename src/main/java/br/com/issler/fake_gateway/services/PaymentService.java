package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.database.repositories.IPaymentRepository;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final IPaymentRepository paymentRepository;

    public PaymentEntity process(CreatePaymentDTO createPaymentDTO) {
        // simulate processing the payment and saving it to the database
        // always accept cards and set status to PENDING for PIX payments
        PaymentEntity payment = PaymentEntity.builder()
                .amount(createPaymentDTO.amount())
                .payerCpf(createPaymentDTO.payerCpf())
                .paymentMethod(createPaymentDTO.paymentMethod())
                .status( createPaymentDTO.paymentMethod().equals(PaymentMethodType.PIX) ?
                        PaymentStatusType.PENDING : PaymentStatusType.ACCEPTED)
                .build();

        return paymentRepository.save(payment);

    }
}
