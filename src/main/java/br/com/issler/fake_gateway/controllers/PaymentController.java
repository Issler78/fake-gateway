package br.com.issler.fake_gateway.controllers;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.dtos.PaymentResponse;
import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public PaymentResponse process(@Valid @RequestBody CreatePaymentDTO createPaymentDTO) throws Exception {
        PaymentEntity payment = paymentService.process(createPaymentDTO);

        return PaymentResponse.builder()
                .gatewayPaymentId(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .qrCode(payment.getPaymentMethod()
                        .equals(PaymentMethodType.PIX) ? "pix_qr_code" : null)
                .build();

    }
}
