package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCustomerProfileRequest {

    @NotBlank(message = "Informe seu nome")
    @Size(max = 255)
    private String name;

    @Size(max = 30)
    private String phone;

    /** VEGETARIAN, VEGAN, GLUTEN_FREE, LACTOSE_FREE. Lista vazia remove as preferencias. */
    private List<String> foodPreferences;
}
