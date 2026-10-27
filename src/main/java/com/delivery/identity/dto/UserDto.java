package com.delivery.identity.dto;

import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UUID tenantId;
    private boolean active;
    private java.time.LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
