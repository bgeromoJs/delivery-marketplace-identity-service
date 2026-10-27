package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "Envie o token do Google")
    private String idToken;
}
