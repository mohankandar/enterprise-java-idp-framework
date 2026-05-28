package io.github.mohankandar.idp.partner.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OAuthClientCredentialsTokenService {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientCredentialsTokenService.class);
    private static final long EXPIRY_SKEW_SECONDS = 30L;

    private final PartnerServiceRegistry registry;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final Map<String, TokenHolder> cache = new ConcurrentHashMap<>();

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Injected ObjectMapper is a framework-managed collaborator reference, not mutable domain state."
    )
    public OAuthClientCredentialsTokenService(PartnerServiceRegistry registry, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.registry = registry;
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    public String getBearerToken(String partnerName) {
        TokenHolder cached = cache.get(partnerName);
        if (cached != null && !cached.isExpired()) {
            return cached.accessToken;
        }
        synchronized (cache.computeIfAbsent(partnerName, k -> TokenHolder.expired())) {
            TokenHolder again = cache.get(partnerName);
            if (again != null && !again.isExpired()) {
                return again.accessToken;
            }
            TokenHolder refreshed = fetchToken(partnerName);
            cache.put(partnerName, refreshed);
            return refreshed.accessToken;
        }
    }

    private TokenHolder fetchToken(String partnerName) {
        IdpPartnerProperties.RestProperties rest = registry.rest(partnerName);
        IdpPartnerProperties.RestAuthProperties auth = rest.getAuth();
        IdpPartnerProperties.OAuthClientCredentialsProperties oauth = auth.resolveOauthClientCredentials();

        String form = "grant_type=client_credentials"
            + "&client_id=" + enc(oauth.getClientId())
            + "&client_secret=" + enc(oauth.getClientSecret());
        if (StringUtils.hasText(oauth.getScope())) {
            form += "&scope=" + enc(oauth.getScope());
        }
        if (StringUtils.hasText(oauth.getAudience())) {
            form += "&audience=" + enc(oauth.getAudience());
        }

        String body = webClientBuilder.clone()
            .build()
            .post()
            .uri(oauth.resolveTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(form)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        if (!StringUtils.hasText(body)) {
            throw new IllegalStateException("Empty token response for partner '" + partnerName + "'");
        }

        try {
            JsonNode json = objectMapper.readTree(body);
            String accessToken = json.path("access_token").asText(null);
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("Missing access_token in token response for partner '" + partnerName + "'");
            }
            long expiresIn = json.path("expires_in").asLong(300L);
            Instant expiresAt = Instant.now().plusSeconds(Math.max(60L, expiresIn - EXPIRY_SKEW_SECONDS));
            log.debug("Fetched OAuth client-credentials token for partner={} expiresIn={}s", partnerName, expiresIn);
            return new TokenHolder(accessToken, expiresAt);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse token response for partner '" + partnerName + "'", e);
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static final class TokenHolder {
        private final String accessToken;
        private final Instant expiresAt;

        private TokenHolder(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return accessToken == null || expiresAt == null || Instant.now().isAfter(expiresAt);
        }

        private static TokenHolder expired() {
            return new TokenHolder(null, Instant.EPOCH);
        }
    }
}
