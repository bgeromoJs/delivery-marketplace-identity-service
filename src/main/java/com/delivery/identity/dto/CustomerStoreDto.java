package com.delivery.identity.dto;

import com.delivery.identity.model.CustomerStore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Loja que o cliente abriu pelo link e que aparece na lista inicial do app. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStoreDto {

    private UUID id;
    private UUID tenantId;
    private String slug;
    private String name;
    private String logoUrl;
    private LocalDateTime createdAt;

    public static CustomerStoreDto fromEntity(CustomerStore store) {
        return CustomerStoreDto.builder()
                .id(store.getId())
                .tenantId(store.getTenantId())
                .slug(store.getSlug())
                .name(store.getName())
                .logoUrl(store.getLogoUrl())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
