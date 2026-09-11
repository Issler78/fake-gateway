package br.com.issler.fake_gateway.dtos;

import br.com.issler.fake_gateway.enums.PaymentMethodType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatePaymentDTO(
    @NotNull(message = "shouldn't be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "shouldn't be empty")
    @Pattern(regexp = "\\d{11}", message = "CPF must contain exactly 11 digits")
    String payerCpf,

    @NotNull(message = "shouldn't be null")
    PaymentMethodType paymentMethod
) {}
