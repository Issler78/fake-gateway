package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import br.com.issler.fake_gateway.database.models.IdempotencyKeyEntity;
import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.dtos.PaymentResponse;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.exceptions.ErrorResponse;
import br.com.issler.fake_gateway.exceptions.GatewayServiceException;
import br.com.issler.fake_gateway.utils.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProcessor processor;
    private final IdempotencyKeyService idempotencyKeyService;

    public GatewayResponse process(CreatePaymentDTO createPaymentDTO, String idempotencyKey) throws Exception {
        // simulate processing the payment

        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyService.findByKey(UUID.fromString(idempotencyKey));
        if (existingKey.isPresent()) {
            return existingKey.get().getResponse();
        }



        GatewayResponse response;
        try {
            PaymentEntity payment = processor.save(createPaymentDTO);
            response = PaymentResponse.<GatewayResponse>builder()
                    .gatewayPaymentId(payment.getId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .paidAt(payment.getPaidAt())
                    .qrCode(payment.getPaymentMethod()
                            .equals(PaymentMethodType.PIX) ? "pix_qr_code" : null)
                    .build();
        } catch (GatewayServiceException e){
            if(e.getType().isClientError()){
                throw e;
            }

            response = ErrorResponse.fromGatewayException(e);
        }

        idempotencyKeyService.save(UUID.fromString(idempotencyKey), response);



        // schedule payment task here, for commit payment before schedule task
        if(
                (response instanceof PaymentResponse paymentResponse) &&
                (paymentResponse.paymentMethod().equals(PaymentMethodType.PIX))
        ) {
            UUID paymentId = paymentResponse.gatewayPaymentId();
            processor.schedulePaymentConfirmation(paymentId);
        }

        return response;
    }
}
