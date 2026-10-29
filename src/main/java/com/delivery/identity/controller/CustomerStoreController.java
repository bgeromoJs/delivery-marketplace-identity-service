package com.delivery.identity.controller;

import com.delivery.identity.dto.CustomerStoreDto;
import com.delivery.identity.dto.LinkStoreRequest;
import com.delivery.identity.service.CustomerStoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Estabelecimentos do cliente. A loja entra na lista quando o cliente abre o app
 * pelo link do estabelecimento (`deliveryhub://loja/{slug}` ou `/loja/{slug}`).
 */
@Tag(name = "Cliente - lojas", description = "Estabelecimentos vinculados ao cliente pelo link")
@RestController
@RequestMapping("/api/customers/me/stores")
@RequiredArgsConstructor
public class CustomerStoreController {

    private final CustomerStoreService customerStoreService;

    @GetMapping
    public ResponseEntity<List<CustomerStoreDto>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerStoreService.list(userId(jwt)));
    }

    @PostMapping
    public ResponseEntity<CustomerStoreDto> link(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody LinkStoreRequest request) {
        return ResponseEntity.ok(customerStoreService.link(userId(jwt), request.getSlug()));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> unlink(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
        customerStoreService.unlink(userId(jwt), slug);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
