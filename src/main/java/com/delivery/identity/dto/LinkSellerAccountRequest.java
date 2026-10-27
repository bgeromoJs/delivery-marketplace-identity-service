package com.delivery.identity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LinkSellerAccountRequest {
    @NotNull(message = "Loja é obrigatória")
    private UUID tenantId;
}
