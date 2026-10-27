package com.delivery.identity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {
    @NotNull(message = "Informe se a conta deve ficar ativa")
    private Boolean active;
}
