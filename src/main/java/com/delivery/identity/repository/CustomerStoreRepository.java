package com.delivery.identity.repository;

import com.delivery.identity.model.CustomerStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerStoreRepository extends JpaRepository<CustomerStore, UUID> {

    List<CustomerStore> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<CustomerStore> findByUserIdAndSlug(UUID userId, String slug);

    long countByUserId(UUID userId);

    /** Lado da loja: clientes que abriram o app pelo link desta loja. */
    List<CustomerStore> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
