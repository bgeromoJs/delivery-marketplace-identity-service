package com.delivery.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolve o dono do link consultando o tenant-service.
 *
 * <p>O cliente envia apenas o slug (ou a URL); a loja e validada no servidor para que
 * um link inventado nao entre na lista de estabelecimentos. Timeout curto: sem o
 * tenant-service o cliente recebe 503 com mensagem clara em vez de uma loja fantasma.</p>
 */
@Component
@Slf4j
public class TenantStoreLookup {

    @Value("${services.tenant.url:http://localhost:8082}")
    private String tenantServiceUrl;

    private final RestClient restClient = buildRestClient();

    public record StoreInfo(UUID tenantId, String name, String logoUrl) {
    }

    /** Devolve a loja do slug ou vazio quando o slug nao existe neste ambiente. */
    public Optional<StoreInfo> theme(String slug) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(tenantServiceUrl + "/api/tenants/{slug}/theme", slug)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (body == null) {
                return Optional.empty();
            }
            return Optional.of(new StoreInfo(
                    toUuid(body.get("tenantId")),
                    asText(body.get("name")),
                    asText(body.get("logoUrl"))));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        } catch (RestClientException error) {
            log.warn("tenant-service indisponivel ao validar a loja {}: {}", slug, error.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nao foi possivel validar a loja agora. Tente novamente.");
        }
    }

    private static UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private static String asText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static RestClient buildRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder().requestFactory(factory).build();
    }
}
