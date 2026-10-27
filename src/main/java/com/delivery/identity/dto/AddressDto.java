package com.delivery.identity.dto;

import com.delivery.identity.model.CustomerAddress;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddressDto {

    private UUID id;
    private String label;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private String zipCode;
    private String reference;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private String formatted;

    public static AddressDto fromEntity(CustomerAddress address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .district(address.getDistrict())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .reference(address.getReference())
                .isDefault(address.isDefault())
                .formatted(format(address))
                .build();
    }

    private static String format(CustomerAddress address) {
        StringBuilder line = new StringBuilder();
        line.append(address.getStreet()).append(", ").append(address.getNumber());
        if (address.getComplement() != null && !address.getComplement().isBlank()) {
            line.append(" - ").append(address.getComplement());
        }
        if (address.getDistrict() != null && !address.getDistrict().isBlank()) {
            line.append(", ").append(address.getDistrict());
        }
        line.append(" - ").append(address.getCity()).append("/").append(address.getState());
        if (address.getZipCode() != null && !address.getZipCode().isBlank()) {
            line.append(" - CEP ").append(address.getZipCode());
        }
        return line.toString();
    }
}
