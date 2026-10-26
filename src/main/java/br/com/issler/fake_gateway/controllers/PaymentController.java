package br.com.issler.fake_gateway.controllers;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import br.com.issler.fake_gateway.dtos.CreatePaymentDTO;
import br.com.issler.fake_gateway.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<GatewayResponse> process(@Valid @RequestBody CreatePaymentDTO createPaymentDTO, @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        GatewayResponse gatewayResponse = paymentService.process(createPaymentDTO, idempotencyKey);

        return ResponseEntity.ok(gatewayResponse);
    }
}
