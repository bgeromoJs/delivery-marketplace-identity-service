package com.delivery.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
public class PlatformSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Integer paymentProposalValidityHours = 24;

    @Column(nullable = false)
    private Integer orderWithoutCourierAlertMinutes = 12;

    @Column(nullable = false)
    private String storeLinkDomain = "deliveryhub.com.br";
}
