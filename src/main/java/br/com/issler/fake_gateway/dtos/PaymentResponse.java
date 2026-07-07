package br.com.issler.fake_gateway.dtos;

import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID paymentId,
        BigDecimal amount,
        PaymentStatusType status,
        PaymentMethodType paymentMethod,
        String qrCode
) {}
