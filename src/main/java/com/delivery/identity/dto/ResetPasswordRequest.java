package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Celular é obrigatório")
    private String phone;

    @NotBlank(message = "Código é obrigatório")
    @Pattern(regexp = "[0-9]{8}", message = "O código deve ter 8 dígitos")
    private String code;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;
}
