package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditCreateRequest {
    @NotBlank
    private String action;
    @NotBlank
    private String target;
    private String description;
}
