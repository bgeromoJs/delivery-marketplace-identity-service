package com.delivery.identity.config;

import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import com.delivery.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "demo-data", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-phone:}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Populando usuários padrão de teste...");

            UUID tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111");

            userRepository.save(User.builder()
                    .name("Administrador da Plataforma")
                    .email("admin@delivery.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone(adminPhone == null || adminPhone.isBlank() ? null : adminPhone.trim())
                    .role(Role.ADMIN)
                    .build());

            userRepository.save(User.builder()
                    .name("Suporte da Plataforma")
                    .email("suporte@delivery.com")
                    .password(passwordEncoder.encode("suporte123"))
                    .phone("(11) 99999-0005")
                    .role(Role.SUPPORT)
                    .build());

            userRepository.save(User.builder()
                    .name("João da Silva (Pizzaria)")
                    .email("joao@pizzaria.com")
                    .password(passwordEncoder.encode("loja123"))
                    .phone("(11) 99999-0002")
                    .role(Role.SELLER)
                    .tenantId(tenantId)
                    .build());

            userRepository.save(User.builder()
                    .name("Carlos Entregador")
                    .email("carlos@moto.com")
                    .password(passwordEncoder.encode("entrega123"))
                    .phone("(11) 99999-0003")
                    .role(Role.COURIER)
                    .build());

            userRepository.save(User.builder()
                    .name("Maria Cliente")
                    .email("maria@gmail.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .phone("(11) 99999-0004")
                    .role(Role.CUSTOMER)
                    .build());

            log.info("Usuários padrão criados com sucesso!");
        }
    }
}
