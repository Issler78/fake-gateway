package br.com.issler.fake_gateway.database.models;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        indexes = {
                @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response", nullable = false)
    private GatewayResponse response;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
