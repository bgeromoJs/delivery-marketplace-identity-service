package com.delivery.identity.dto;

import com.delivery.identity.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerProfileDto {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private List<String> foodPreferences;
    private boolean googleLinked;
    private LocalDateTime createdAt;

    public static CustomerProfileDto fromEntity(User user) {
        return CustomerProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .foodPreferences(splitPreferences(user.getFoodPreferences()))
                .googleLinked(user.getGoogleSub() != null && !user.getGoogleSub().isBlank())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static List<String> splitPreferences(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }
}
