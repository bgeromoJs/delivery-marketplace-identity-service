package com.delivery.identity.dto;

import com.delivery.identity.model.AdminAuditEntry;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class AuditEntryDto {
    UUID id;
    UUID actorUserId;
    String actorEmail;
    String action;
    String target;
    String description;
    LocalDateTime createdAt;

    public static AuditEntryDto fromEntity(AdminAuditEntry entry) {
        return AuditEntryDto.builder()
                .id(entry.getId())
                .actorUserId(entry.getActorUserId())
                .actorEmail(entry.getActorEmail())
                .action(entry.getAction())
                .target(entry.getTarget())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
