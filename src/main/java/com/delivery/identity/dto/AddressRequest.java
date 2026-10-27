package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Escolha um rotulo para o endereco")
    @Size(max = 40)
    private String label;

    @NotBlank(message = "Informe a rua")
    @Size(max = 255)
    private String street;

    @NotBlank(message = "Informe o numero")
    @Size(max = 30)
    private String number;

    @Size(max = 120)
    private String complement;

    @Size(max = 120)
    private String district;

    @NotBlank(message = "Informe a cidade")
    @Size(max = 120)
    private String city;

    @NotBlank(message = "Informe o estado")
    @Size(min = 2, max = 2, message = "Use a sigla do estado com 2 letras")
    private String state;

    @Size(max = 9)
    private String zipCode;

    @Size(max = 255)
    private String reference;

    private Boolean isDefault;
}
