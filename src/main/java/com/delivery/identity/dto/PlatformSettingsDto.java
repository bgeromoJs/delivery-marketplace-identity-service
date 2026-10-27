package com.delivery.identity.dto;

import com.delivery.identity.model.PlatformSettings;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlatformSettingsDto {
    @NotNull @Min(1)
    private Integer paymentProposalValidityHours;
    @NotNull @Min(1)
    private Integer orderWithoutCourierAlertMinutes;
    @NotBlank
    private String storeLinkDomain;

    public static PlatformSettingsDto fromEntity(PlatformSettings settings) {
        PlatformSettingsDto dto = new PlatformSettingsDto();
        dto.setPaymentProposalValidityHours(settings.getPaymentProposalValidityHours());
        dto.setOrderWithoutCourierAlertMinutes(settings.getOrderWithoutCourierAlertMinutes());
        dto.setStoreLinkDomain(settings.getStoreLinkDomain());
        return dto;
    }

    public void applyTo(PlatformSettings settings) {
        settings.setPaymentProposalValidityHours(paymentProposalValidityHours);
        settings.setOrderWithoutCourierAlertMinutes(orderWithoutCourierAlertMinutes);
        settings.setStoreLinkDomain(storeLinkDomain.trim());
    }
}
