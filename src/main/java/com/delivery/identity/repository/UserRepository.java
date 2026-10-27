package com.delivery.identity.repository;

import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleSub(String googleSub);
    List<User> findAllByOrderByCreatedAtDesc();
    boolean existsByEmail(String email);
    long countByRoleAndActiveTrue(Role role);
}
