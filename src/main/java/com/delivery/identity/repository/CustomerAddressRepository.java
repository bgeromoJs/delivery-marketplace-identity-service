package com.delivery.identity.repository;

import com.delivery.identity.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByUserIdOrderByCreatedAtAsc(UUID userId);
    Optional<CustomerAddress> findByIdAndUserId(UUID id, UUID userId);
    Optional<CustomerAddress> findFirstByUserIdAndIsDefaultTrue(UUID userId);
    long countByUserId(UUID userId);
}
