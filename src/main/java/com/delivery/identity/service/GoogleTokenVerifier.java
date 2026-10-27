package com.delivery.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Valida o ID token emitido pelo Google Sign-In no Android.
 *
 * O app envia um JWT assinado pelo Google; aqui conferimos assinatura (JWKS publico),
 * expiracao, emissor, audiencia (client ID) e e-mail verificado. Nenhum segredo do
 * Google e necessario no app: o client ID e publico.
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private final String clientId;
    private final String issuer;
    private final JwtDecoder decoder;

    public GoogleTokenVerifier(
            @Value("${app.google.client-id:}") String clientId,
            @Value("${app.google.issuer:https://accounts.google.com}") String issuer,
            @Value("${app.google.jwk-set-uri:https://www.googleapis.com/oauth2/v3/certs}") String jwkSetUri) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.issuer = issuer;
        this.decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    public boolean isConfigured() {
        return !clientId.isBlank();
    }

    public GoogleIdentity verify(String idToken) {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O login com Google ainda nao foi configurado neste servidor");
        }
        Jwt jwt;
        try {
            jwt = decoder.decode(idToken);
        } catch (JwtException exception) {
            log.debug("ID token do Google recusado: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao foi possivel validar o login com Google");
        }

        String tokenIssuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
        if (!"accounts.google.com".equals(tokenIssuer) && !"https://accounts.google.com".equals(tokenIssuer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Emissor do token Google invalido");
        }
        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(clientId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google emitido para outro aplicativo");
        }
        String email = jwt.getClaimAsString("email");
        Object verified = jwt.getClaim("email_verified");
        boolean emailVerified = Boolean.TRUE.equals(verified) || "true".equals(String.valueOf(verified));
        if (email == null || email.isBlank() || !emailVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A conta Google precisa ter e-mail verificado");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google sem identificador de conta");
        }
        return new GoogleIdentity(subject, email.toLowerCase().trim(),
                jwt.getClaimAsString("name"), jwt.getClaimAsString("picture"));
    }

    public record GoogleIdentity(String subject, String email, String name, String picture) {
    }
}
