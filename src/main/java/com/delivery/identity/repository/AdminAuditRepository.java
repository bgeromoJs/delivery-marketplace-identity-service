package com.delivery.identity.repository;

import com.delivery.identity.model.AdminAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminAuditRepository extends JpaRepository<AdminAuditEntry, UUID> {
    List<AdminAuditEntry> findTop100ByOrderByCreatedAtDesc();
}
