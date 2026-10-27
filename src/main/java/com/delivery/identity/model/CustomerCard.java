package com.delivery.identity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cartao salvo apenas como referencia: bandeira, quatro ultimos digitos, titular e validade.
 * O numero completo e o CVV nunca sao enviados nem persistidos.
 */
@Entity
@Table(name = "customer_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 30)
    private String brand;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
