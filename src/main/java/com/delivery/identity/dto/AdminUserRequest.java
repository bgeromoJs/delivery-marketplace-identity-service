package com.delivery.identity.dto;

import com.delivery.identity.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Cadastro/edição de usuário feito pelo painel administrativo.
 * Diferente do registro público, permite escolher qualquer perfil e o tenant.
 */
@Data
public class AdminUserRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private String phone;

    @NotNull(message = "Perfil (Role) é obrigatório")
    private Role role;

    /** Loja do usuário. Obrigatório para SELLER; ignorado para ADMIN/SUPPORT. */
    private UUID tenantId;

    /** Obrigatória na criação; na edição, em branco mantém a senha atual. */
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;

    /** Na criação, ausente equivale a true. */
    private Boolean active;
}
