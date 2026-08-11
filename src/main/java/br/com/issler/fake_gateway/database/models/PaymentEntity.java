package br.com.issler.fake_gateway.database.models;

import br.com.issler.fake_gateway.enums.PaymentMethodType;
import br.com.issler.fake_gateway.enums.PaymentStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatusType status;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethod;

    @Column(name = "payer_cpf", nullable = false, columnDefinition = "CHAR(11)")
    private String payerCpf;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
