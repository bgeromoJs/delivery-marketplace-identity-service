package com.delivery.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Link ou slug da loja que o cliente abriu. Aceita `vera-marmitex` ou a URL completa. */
@Data
public class LinkStoreRequest {

    @NotBlank(message = "Informe o link da loja")
    private String slug;
}
