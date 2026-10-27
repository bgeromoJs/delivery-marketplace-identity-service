package com.delivery.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_entries")
@Getter
@Setter
@NoArgsConstructor
public class AdminAuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID actorUserId;

    @Column(nullable = false)
    private String actorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 180)
    private String target;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
