package com.delivery.identity.service;

import com.delivery.identity.dto.CustomerStoreDto;
import com.delivery.identity.model.CustomerStore;
import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import com.delivery.identity.repository.CustomerStoreRepository;
import com.delivery.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estabelecimentos do cliente final.
 *
 * <p>O app nao escolhe a loja no build: ela entra na conta quando o cliente abre o
 * aplicativo pelo link da loja. A lista resultante e a tela inicial do app quando o
 * cliente tem mais de uma loja; com uma so, o cardapio abre direto.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomerStoreService {

    private static final int MAX_STORES = 50;

    /** Trecho `/loja/{slug}` de `deliveryhub://loja/x` ou `https://host/loja/x`. */
    private static final Pattern LINK_PATTERN = Pattern.compile("/loja/([A-Za-z0-9][A-Za-z0-9_-]*)");
    private static final Pattern SLUG_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_-]{0,59}");

    private final UserRepository userRepository;
    private final CustomerStoreRepository storeRepository;
    private final TenantStoreLookup tenantLookup;

    public List<CustomerStoreDto> list(UUID userId) {
        requireCustomer(userId);
        return storeRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(CustomerStoreDto::fromEntity)
                .toList();
    }

    /**
     * Vincula a loja do link a conta do cliente. Idempotente: abrir o mesmo link
     * duas vezes devolve o vinculo existente sem duplicar.
     */
    @Transactional
    public CustomerStoreDto link(UUID userId, String rawSlug) {
        requireCustomer(userId);
        String slug = normalizeSlug(rawSlug);
        if (slug == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link da loja invalido");
        }
        return storeRepository.findByUserIdAndSlug(userId, slug)
                .map(CustomerStoreDto::fromEntity)
                .orElseGet(() -> createLink(userId, slug));
    }

    @Transactional
    public void unlink(UUID userId, String rawSlug) {
        requireCustomer(userId);
        String slug = normalizeSlug(rawSlug);
        if (slug == null) {
            return;
        }
        storeRepository.findByUserIdAndSlug(userId, slug).ifPresent(storeRepository::delete);
    }

    private CustomerStoreDto createLink(UUID userId, String slug) {
        if (storeRepository.countByUserId(userId) >= MAX_STORES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Voce ja salvou o limite de " + MAX_STORES + " lojas");
        }
        TenantStoreLookup.StoreInfo store = tenantLookup.theme(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Loja nao encontrada neste ambiente. Confira o link."));
        CustomerStore entity = CustomerStore.builder()
                .userId(userId)
                .slug(slug)
                .tenantId(store.tenantId())
                .name(store.name())
                .logoUrl(store.logoUrl())
                .build();
        return CustomerStoreDto.fromEntity(storeRepository.save(entity));
    }

    /** Aceita o slug puro, `deliveryhub://loja/{slug}` ou a URL do site. */
    static String normalizeSlug(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            return null;
        }
        Matcher matcher = LINK_PATTERN.matcher(text);
        String candidate = matcher.find() ? matcher.group(1) : text;
        candidate = candidate.trim().toLowerCase(Locale.ROOT);
        return SLUG_PATTERN.matcher(candidate).matches() ? candidate : null;
    }

    private User requireCustomer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta nao encontrada"));
        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este perfil e exclusivo de contas de cliente");
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta esta desativada");
        }
        return user;
    }
}
