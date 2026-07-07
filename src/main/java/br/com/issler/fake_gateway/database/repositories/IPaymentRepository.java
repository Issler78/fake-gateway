package br.com.issler.fake_gateway.database.repositories;

import br.com.issler.fake_gateway.database.models.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}
