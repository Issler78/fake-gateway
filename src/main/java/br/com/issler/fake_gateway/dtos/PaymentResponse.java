package br.com.issler.fake_gateway.dtos;

import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID gatewayPaymentId,
        BigDecimal amount,
        PaymentStatusType status,
        LocalDateTime paidAt,
        PaymentMethodType paymentMethod,
        String qrCode
) {}
