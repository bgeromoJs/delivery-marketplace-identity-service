package com.delivery.identity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loja vinculada ao cliente pelo link do estabelecimento.
 *
 * <p>Cada vez que o cliente abre o app por um link (`deliveryhub://loja/{slug}` ou
 * `https://.../loja/{slug}`), a loja passa a aparecer na lista inicial dele. O nome e o
 * logo ficam gravados como referencia para quando o tenant-service estiver fora do ar;
 * a lista e recarregada do servidor a cada abertura.</p>
 */
@Entity
@Table(name = "customer_stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerStore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(length = 160)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
