package com.delivery.identity.repository;

import com.delivery.identity.model.CustomerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerCardRepository extends JpaRepository<CustomerCard, UUID> {
    List<CustomerCard> findByUserIdOrderByCreatedAtAsc(UUID userId);
    Optional<CustomerCard> findByIdAndUserId(UUID id, UUID userId);
    Optional<CustomerCard> findFirstByUserIdAndIsDefaultTrue(UUID userId);
    long countByUserId(UUID userId);
}
