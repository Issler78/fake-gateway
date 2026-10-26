package br.com.issler.fake_gateway.services;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import br.com.issler.fake_gateway.database.models.IdempotencyKeyEntity;
import br.com.issler.fake_gateway.database.repositories.IIdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {
    private final IIdempotencyKeyRepository idempotencyKeyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(UUID idempotencyKey, GatewayResponse response) {
        IdempotencyKeyEntity entity = IdempotencyKeyEntity.builder()
                .idempotencyKey(idempotencyKey)
                .response(response)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        idempotencyKeyRepository.save(entity);
    }

    public Optional<IdempotencyKeyEntity> findByKey(UUID idempotencyKey) {
        return idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);
    }
}
