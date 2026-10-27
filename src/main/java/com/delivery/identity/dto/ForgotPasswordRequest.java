package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Celular é obrigatório")
    private String phone;
}
