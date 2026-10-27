package com.delivery.identity.controller;

import com.delivery.identity.dto.AddressDto;
import com.delivery.identity.dto.AddressRequest;
import com.delivery.identity.dto.CardDto;
import com.delivery.identity.dto.CardRequest;
import com.delivery.identity.dto.CustomerProfileDto;
import com.delivery.identity.dto.UpdateCustomerProfileRequest;
import com.delivery.identity.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Cliente", description = "Perfil, preferencias, enderecos e cartoes do cliente final")
@RestController
@RequestMapping("/api/customers/me")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerProfileService customerProfileService;

    @GetMapping
    public ResponseEntity<CustomerProfileDto> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerProfileService.getProfile(userId(jwt)));
    }

    @PutMapping
    public ResponseEntity<CustomerProfileDto> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return ResponseEntity.ok(customerProfileService.updateProfile(userId(jwt), request));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> listAddresses(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerProfileService.listAddresses(userId(jwt)));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> createAddress(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(customerProfileService.createAddress(userId(jwt), request));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> updateAddress(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID id,
                                                    @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(customerProfileService.updateAddress(userId(jwt), id, request));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        customerProfileService.deleteAddress(userId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<AddressDto> setDefaultAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ResponseEntity.ok(customerProfileService.setDefaultAddress(userId(jwt), id));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardDto>> listCards(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerProfileService.listCards(userId(jwt)));
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDto> createCard(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody CardRequest request) {
        return ResponseEntity.ok(customerProfileService.createCard(userId(jwt), request));
    }

    @PatchMapping("/cards/{id}/default")
    public ResponseEntity<CardDto> setDefaultCard(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ResponseEntity.ok(customerProfileService.setDefaultCard(userId(jwt), id));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        customerProfileService.deleteCard(userId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
