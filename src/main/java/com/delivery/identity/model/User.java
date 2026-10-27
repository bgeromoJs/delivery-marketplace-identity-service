package com.delivery.identity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private UUID tenantId;

    /** Identificador estável da conta Google (`sub`), quando o login social está vinculado. */
    @Column(name = "google_sub", unique = true)
    private String googleSub;

    /** Preferências alimentares do cliente, separadas por vírgula (VEGETARIAN, VEGAN, GLUTEN_FREE, LACTOSE_FREE). */
    @Column(name = "food_preferences")
    private String foodPreferences;

    /** Conta desativada não consegue autenticar, mas o histórico é preservado. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
