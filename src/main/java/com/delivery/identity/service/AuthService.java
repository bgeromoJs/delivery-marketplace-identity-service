package com.delivery.identity.service;

import com.delivery.identity.dto.AuthResponse;
import com.delivery.identity.dto.AdminUserRequest;
import com.delivery.identity.dto.GoogleLoginRequest;
import com.delivery.identity.dto.LoginRequest;
import com.delivery.identity.dto.RegisterRequest;
import com.delivery.identity.dto.UserDto;
import com.delivery.identity.dto.UpdateUserRequest;
import com.delivery.identity.dto.ForgotPasswordRequest;
import com.delivery.identity.dto.ForgotPasswordResponse;
import com.delivery.identity.dto.ResetPasswordRequest;
import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import com.delivery.identity.model.PasswordResetToken;
import com.delivery.identity.repository.UserRepository;
import com.delivery.identity.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final TwilioSmsService smsService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.security.jwt.secret}")
    private String resetCodePepper;

    @Value("${spring.security.jwt.issuer}")
    private String tokenIssuer;

    @Value("${spring.security.jwt.ttl-minutes:60}")
    private long tokenTtlMinutes;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordMatches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta está desativada. Procure um administrador.");
        }
        if (!user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        String token = generateToken(user);
        return AuthResponse.of(token, UserDto.fromEntity(user));
    }

    /**
     * Login/cadastro com Google. Contas de loja, suporte e administracao nao entram pelo app do cliente.
     * Quando o e-mail ja existe como CUSTOMER, o vinculo Google e adicionado a conta existente.
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleIdentity identity = googleTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByGoogleSub(identity.subject()).orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(identity.email()).orElse(null);
        }

        if (user == null) {
            String displayName = identity.name() == null || identity.name().isBlank()
                    ? identity.email()
                    : identity.name().trim();
            user = User.builder()
                    .name(displayName)
                    .email(identity.email())
                    // Senha aleatoria e inutilizavel: a conta e acessada apenas pelo Google.
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.CUSTOMER)
                    .tenantId(null)
                    .active(true)
                    .googleSub(identity.subject())
                    .build();
        } else {
            if (user.getRole() != Role.CUSTOMER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Esta conta nao e de cliente. Use o aplicativo correspondente ao seu perfil.");
            }
            if (!user.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Esta conta esta desativada. Procure o suporte.");
            }
            if (user.getGoogleSub() != null && !user.getGoogleSub().equals(identity.subject())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Este e-mail ja esta vinculado a outra conta Google");
            }
            if (user.getGoogleSub() == null) {
                user.setGoogleSub(identity.subject());
            }
        }

        user = userRepository.save(user);
        return AuthResponse.of(generateToken(user), UserDto.fromEntity(user));
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {        if (!smsService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O envio de SMS ainda não foi configurado.");
        }
        String requestedPhone = normalizePhone(request.getPhone());
        if (requestedPhone.isBlank()) {
            return new ForgotPasswordResponse("Se a conta existir e tiver celular cadastrado, enviaremos um código por SMS.");
        }
        User user = userRepository.findAll().stream()
                .filter(candidate -> candidate.getRole() == com.delivery.identity.model.Role.ADMIN)
                .filter(candidate -> requestedPhone.equals(normalizePhone(candidate.getPhone())))
                .findFirst().orElse(null);

        if (user == null) {
            return new ForgotPasswordResponse("Se a conta existir e tiver celular cadastrado, enviaremos um código por SMS.");
        }

        String destination = toE164Brazil(user.getPhone());
        if (destination == null) {
            return new ForgotPasswordResponse("Se a conta existir e tiver celular cadastrado, enviaremos um código por SMS.");
        }

        Instant now = Instant.now();
        PasswordResetToken currentCode = passwordResetTokenRepository.findFirstByUserId(user.getId()).orElse(null);
        if (currentCode != null && currentCode.getExpiresAt().isAfter(now)
                && currentCode.getCreatedAt() != null && currentCode.getCreatedAt().plusSeconds(60).isAfter(now)) {
            return new ForgotPasswordResponse("Se a conta existir e tiver celular cadastrado, enviaremos um código por SMS.");
        }

        String code = String.format(Locale.ROOT, "%08d", secureRandom.nextInt(100_000_000));
        Instant expiresAt = now.plus(10, ChronoUnit.MINUTES);
        passwordResetTokenRepository.deleteAllByUserId(user.getId());
        passwordResetTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        passwordResetTokenRepository.save(new PasswordResetToken(hashCode(code), user.getId(), expiresAt));
        try {
            smsService.send(destination, "Seu código para redefinir a senha do painel é " + code + ". Ele expira em 10 minutos.");
        } catch (TwilioSmsService.SmsDeliveryException exception) {
            log.warn("Falha no envio do SMS de redefinição para a conta solicitante: {}", exception.getMessage());
            passwordResetTokenRepository.deleteAllByUserId(user.getId());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o SMS. Verifique o número cadastrado e a configuração do provedor.");
        }
        return new ForgotPasswordResponse("Se a conta existir e tiver celular cadastrado, enviaremos um código por SMS.");
    }

    public void resetPassword(ResetPasswordRequest request) {
        String requestedPhone = normalizePhone(request.getPhone());
        if (requestedPhone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Solicite um novo código.");
        }
        User user = userRepository.findAll().stream()
                .filter(candidate -> candidate.getRole() == com.delivery.identity.model.Role.ADMIN)
                .filter(candidate -> requestedPhone.equals(normalizePhone(candidate.getPhone())))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Solicite um novo código."));
        PasswordResetToken reset = passwordResetTokenRepository.findFirstByUserId(user.getId()).orElse(null);
        if (reset == null || reset.getExpiresAt().isBefore(Instant.now())) {
            if (reset != null) passwordResetTokenRepository.delete(reset);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Solicite um novo código.");
        }
        if (reset.getAttemptsRemaining() <= 0) {
            passwordResetTokenRepository.delete(reset);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Solicite um novo código.");
        }
        if (!hashCode(request.getCode()).equals(reset.getCodeHash())) {
            reset.setAttemptsRemaining(reset.getAttemptsRemaining() - 1);
            if (reset.getAttemptsRemaining() == 0) passwordResetTokenRepository.delete(reset);
            else passwordResetTokenRepository.save(reset);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado. Verifique o código enviado por SMS.");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(reset);
    }

    private String hashCode(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(resetCodePepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível proteger o código de redefinição", e);
        }
    }

    private String toE164Brazil(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String trimmed = phone.trim();
        String digits = trimmed.replaceAll("\\D", "");
        String normalized = trimmed.startsWith("+") ? "+" + digits : digits.startsWith("55") ? "+" + digits : "+55" + digits;
        return normalized.matches("\\+[1-9][0-9]{7,14}") ? normalized : null;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() > 11) digits = digits.substring(2);
        return digits;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O cadastro público cria apenas contas de cliente. Vendedor, entregador, suporte e administrador são cadastrados pelo painel administrativo.");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                // O cliente é global; o vínculo com uma loja, quando necessário, é definido pelo painel.
                .tenantId(null)
                .active(true)
                .build();

        user = userRepository.save(user);
        String token = generateToken(user);
        return AuthResponse.of(token, UserDto.fromEntity(user));
    }

    @Transactional
    public UserDto createManagedUser(AdminUserRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        validateRoleAndTenant(request.getRole(), request.getTenantId());
        requireStrongPassword(request.getPassword(), true);

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(trimToNull(request.getPhone()))
                .role(request.getRole())
                .tenantId(tenantFor(request.getRole(), request.getTenantId()))
                .active(request.getActive() == null || request.getActive())
                .build();
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto updateManagedUser(UUID id, AdminUserRequest request, UUID actorId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"); });
        validateRoleAndTenant(request.getRole(), request.getTenantId());
        requireStrongPassword(request.getPassword(), false);

        boolean active = request.getActive() == null ? user.isActive() : request.getActive();
        ensureAdminInvariants(user, request.getRole(), active, actorId);

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPhone(trimToNull(request.getPhone()));
        user.setRole(request.getRole());
        user.setTenantId(tenantFor(request.getRole(), request.getTenantId()));
        user.setActive(active);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto setManagedUserActive(UUID id, boolean active, UUID actorId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        ensureAdminInvariants(user, user.getRole(), active, actorId);
        user.setActive(active);
        return UserDto.fromEntity(userRepository.save(user));
    }

    private void validateRoleAndTenant(Role role, UUID tenantId) {
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o perfil do usuário");
        }
        if ((role == Role.ADMIN || role == Role.SUPPORT) && tenantId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Perfis de administrador e suporte são da plataforma e não podem ser vinculados a uma loja");
        }
        if (role == Role.SELLER && tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione a loja do vendedor");
        }
    }

    private UUID tenantFor(Role role, UUID tenantId) {
        return role == Role.ADMIN || role == Role.SUPPORT ? null : tenantId;
    }

    private void requireStrongPassword(String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Defina uma senha inicial com pelo menos 8 caracteres");
            }
            return;
        }
        if (password.trim().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 8 caracteres");
        }
    }

    /**
     * Impede que a última conta ADMIN ativa seja desativada ou rebaixada,
     * e que o próprio administrador remova o seu acesso.
     */
    private void ensureAdminInvariants(User target, Role newRole, boolean newActive, UUID actorId) {
        boolean losesAdminAccess = target.getRole() == Role.ADMIN && target.isActive()
                && (newRole != Role.ADMIN || !newActive);
        if (!losesAdminAccess) {
            return;
        }
        if (target.getId().equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você não pode remover o próprio acesso de administrador");
        }
        if (userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "É necessário manter pelo menos um administrador ativo");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UserDto registerTeamAdmin(RegisterRequest request) {
        if (request.getRole() != com.delivery.identity.model.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membros da equipe do painel devem ter perfil ADMIN");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .tenantId(null)
                .build();
        return UserDto.fromEntity(userRepository.save(user));
    }

    public UserDto linkSellerAccount(UUID userId, UUID tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
        if (user.getRole() == com.delivery.identity.model.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Uma conta de administrador não pode ser vinculada como vendedor.");
        }
        if (user.getTenantId() != null && !user.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conta já está vinculada a outra loja.");
        }
        user.setRole(com.delivery.identity.model.Role.SELLER);
        user.setTenantId(tenantId);
        return UserDto.fromEntity(userRepository.save(user));
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return UserDto.fromEntity(user);
    }

    public List<UserDto> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream().map(UserDto::fromEntity).toList();
    }

    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new RuntimeException("Email já cadastrado"); });
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
        return UserDto.fromEntity(userRepository.save(user));
    }

    private String generateToken(User user) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(tokenIssuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(tokenTtlMinutes, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }
}
