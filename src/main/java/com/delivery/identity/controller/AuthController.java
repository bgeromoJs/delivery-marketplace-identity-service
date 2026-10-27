package com.delivery.identity.controller;

import com.delivery.identity.dto.AuthResponse;
import com.delivery.identity.dto.AdminUserRequest;
import com.delivery.identity.dto.GoogleLoginRequest;
import com.delivery.identity.dto.LoginRequest;
import com.delivery.identity.dto.RegisterRequest;
import com.delivery.identity.dto.UserDto;
import com.delivery.identity.dto.UserStatusRequest;
import com.delivery.identity.dto.UpdateUserRequest;
import com.delivery.identity.dto.PlatformSettingsDto;
import com.delivery.identity.dto.ForgotPasswordRequest;
import com.delivery.identity.dto.ResetPasswordRequest;
import com.delivery.identity.dto.ForgotPasswordResponse;
import com.delivery.identity.dto.AuditCreateRequest;
import com.delivery.identity.dto.AuditEntryDto;
import com.delivery.identity.dto.LinkSellerAccountRequest;
import com.delivery.identity.service.AuthService;
import com.delivery.identity.service.AdminConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;
import java.util.List;

@Tag(name = "Autenticação", description = "Login, cadastro, recuperação de senha e gestão de usuários")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminConfigurationService adminConfigurationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(authService.getUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.updateUser(id, request);
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt), "MEMBRO_EQUIPE_ATUALIZADO", user.getEmail());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/admin/team")
    public ResponseEntity<UserDto> createTeamMember(@Valid @RequestBody RegisterRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.registerTeamAdmin(request);
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt), "MEMBRO_EQUIPE_CRIADO", user.getEmail());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> createManagedUser(@Valid @RequestBody AdminUserRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.createManagedUser(request);
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt), "USUARIO_CRIADO",
                user.getEmail() + " (" + user.getRole() + ")");
        return ResponseEntity.ok(user);
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<UserDto> updateManagedUser(@PathVariable UUID id,
                                                      @Valid @RequestBody AdminUserRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.updateManagedUser(id, request, actorId(jwt));
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt), "USUARIO_ATUALIZADO",
                user.getEmail() + " (" + user.getRole() + ")");
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/admin/users/{id}/status")
    public ResponseEntity<UserDto> setManagedUserActive(@PathVariable UUID id,
                                                         @Valid @RequestBody UserStatusRequest request,
                                                         @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.setManagedUserActive(id, request.getActive(), actorId(jwt));
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt),
                user.isActive() ? "USUARIO_ATIVADO" : "USUARIO_DESATIVADO", user.getEmail());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/admin/users/{id}/seller-link")
    public ResponseEntity<UserDto> linkSellerAccount(@PathVariable UUID id,
            @Valid @RequestBody LinkSellerAccountRequest request, @AuthenticationPrincipal Jwt jwt) {
        UserDto user = authService.linkSellerAccount(id, request.getTenantId());
        adminConfigurationService.recordTeamChange(actorId(jwt), actorEmail(jwt), "VENDEDOR_VINCULADO_LOJA", user.getEmail());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/admin/settings")
    public ResponseEntity<PlatformSettingsDto> getSettings() {
        return ResponseEntity.ok(adminConfigurationService.getSettings());
    }

    @PutMapping("/admin/settings")
    public ResponseEntity<PlatformSettingsDto> updateSettings(@Valid @RequestBody PlatformSettingsDto request,
                                                               @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(adminConfigurationService.updateSettings(request, actorId(jwt), actorEmail(jwt)));
    }

    @GetMapping("/admin/audit")
    public ResponseEntity<List<AuditEntryDto>> listAuditEntries() {
        return ResponseEntity.ok(adminConfigurationService.getAuditEntries());
    }

    @PostMapping("/admin/audit")
    public ResponseEntity<AuditEntryDto> createAuditEntry(@Valid @RequestBody AuditCreateRequest request,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(adminConfigurationService.createAuditEntry(request, actorId(jwt), actorEmail(jwt)));
    }

    private UUID actorId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    private String actorEmail(Jwt jwt) { return jwt.getClaimAsString("email"); }
}
