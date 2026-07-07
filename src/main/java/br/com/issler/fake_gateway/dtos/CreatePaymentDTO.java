package br.com.issler.fake_gateway.dtos;

import br.com.issler.fake_gateway.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatePaymentDTO(
    @NotNull(message = "shouldn't be null")
    BigDecimal amount,

    @NotBlank(message = "shouldn't be empty")
    @Size(min = 11, max = 11)
    String payerCpf,

    @NotNull(message = "shouldn't be null")
    PaymentMethodType paymentMethod
) {}
