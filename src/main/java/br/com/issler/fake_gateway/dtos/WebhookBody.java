package br.com.issler.fake_gateway.dtos;

import br.com.issler.fake_gateway.enums.PaymentStatusType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record WebhookBody(
        UUID gatewayPaymentId,
        LocalDateTime paidAt, // on a real project, paidAt can be null (because, sometimes payment not is accepted). For study purposes, it always has a value
        PaymentStatusType status
) {}
