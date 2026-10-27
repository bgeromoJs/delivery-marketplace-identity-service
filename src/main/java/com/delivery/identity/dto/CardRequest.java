package com.delivery.identity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CardRequest {

    @NotBlank(message = "Informe a bandeira do cartao")
    @Size(max = 30)
    private String brand;

    @NotBlank(message = "Informe os quatro ultimos digitos")
    @Pattern(regexp = "\\d{4}", message = "Informe exatamente os quatro ultimos digitos")
    private String last4;

    @NotBlank(message = "Informe o nome impresso no cartao")
    @Size(max = 120)
    private String holderName;

    @NotNull(message = "Informe o mes de validade")
    @Min(value = 1, message = "Mes invalido")
    @Max(value = 12, message = "Mes invalido")
    private Integer expiryMonth;

    @NotNull(message = "Informe o ano de validade")
    @Min(value = 2024, message = "Ano invalido")
    @Max(value = 2100, message = "Ano invalido")
    private Integer expiryYear;

    private Boolean isDefault;
}
