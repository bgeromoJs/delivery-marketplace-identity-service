package com.delivery.identity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Rotulo escolhido pelo cliente: Casa, Trabalho, Outro. */
    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 30)
    private String number;

    @Column(length = 120)
    private String complement;

    @Column(length = 120)
    private String district;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "zip_code", length = 9)
    private String zipCode;

    @Column(length = 255)
    private String reference;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
