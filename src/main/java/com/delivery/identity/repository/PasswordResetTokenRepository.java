package com.delivery.identity.repository;

import com.delivery.identity.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findFirstByUserId(UUID userId);
    void deleteAllByUserId(UUID userId);
    void deleteAllByExpiresAtBefore(Instant instant);
}
