package com.delivery.identity.dto;

import com.delivery.identity.model.CustomerCard;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CardDto {

    private UUID id;
    private String brand;
    private String last4;
    private String holderName;
    private Integer expiryMonth;
    private Integer expiryYear;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private String maskedNumber;

    public static CardDto fromEntity(CustomerCard card) {
        return CardDto.builder()
                .id(card.getId())
                .brand(card.getBrand())
                .last4(card.getLast4())
                .holderName(card.getHolderName())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .isDefault(card.isDefault())
                .maskedNumber("**** **** **** " + card.getLast4())
                .build();
    }
}
