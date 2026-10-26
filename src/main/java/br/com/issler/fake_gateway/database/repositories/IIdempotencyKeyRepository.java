package br.com.issler.fake_gateway.database.repositories;

import br.com.issler.fake_gateway.database.models.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, UUID> {
    Optional<IdempotencyKeyEntity> findByIdempotencyKey(UUID idempotencyKey);
}
