package io.github.mohankandar.idp.identity.http;

import io.github.mohankandar.idp.identity.IdpIdentity;
import io.github.mohankandar.idp.identity.IdpIdentityClient;
import io.github.mohankandar.idp.identity.IdpIdentityProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
public class HttpIdentityClient implements IdpIdentityClient {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    private final String baseUrl;
    private final String pathTemplate;
    private final String bearer;
    private final long timeoutMs;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Injected WebClient is a framework-managed collaborator reference, not mutable domain state."
    )
    public HttpIdentityClient(RestTemplateBuilder builder, @Nullable WebClient webClient, IdpIdentityProperties props) {
        this.timeoutMs = props.getTimeoutMs();
        this.baseUrl = props.getBaseUrl();
        this.pathTemplate = props.getPathTemplate();
        this.bearer = props.getBearer();

        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(this.timeoutMs))
                .setReadTimeout(Duration.ofMillis(this.timeoutMs))
                .build();
        this.webClient = webClient;
    }

    @Override
    public Optional<IdpIdentity> current() {
        return Optional.empty();
    }

    private static void setAuth(HttpHeaders headers, @Nullable String token) {
        if (StringUtils.hasText(token)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }

    @SuppressWarnings("unchecked")
    private static IdpIdentity mapToIdentity(Map<String, Object> m) {
        if (m == null) {
            return null;
        }

        String networkId = String.valueOf(m.getOrDefault("networkId", ""));
        String firstName = (String) m.get("firstName");
        String lastName = (String) m.get("lastName");
        String email = (String) m.get("email");
        List<String> roles = Optional.ofNullable((Collection<?>) m.get("roles"))
                .map(col -> col.stream().map(String::valueOf).toList())
                .orElse(List.of());

        return new IdpIdentity(networkId, firstName, lastName, email, roles, m);
    }

    @Override
    public Optional<IdpIdentity> byNetworkId(String networkId, @Nullable String inboundBearer) {
        if (!StringUtils.hasText(baseUrl)) {
            return Optional.empty();
        }

        String url = baseUrl + pathTemplate.replace("{networkId}", networkId);
        String token = StringUtils.hasText(bearer) ? bearer : inboundBearer;

        try {
            if (webClient != null) {
                Map<String, Object> body = webClient.get()
                        .uri(url)
                        .headers(h -> setAuth(h, token))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(Duration.ofMillis(timeoutMs));
                return Optional.ofNullable(mapToIdentity(body));
            }

            var headers = new org.springframework.http.HttpHeaders();
            setAuth(headers, token);
            var entity = new org.springframework.http.HttpEntity<>(headers);
            var resp = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
            return Optional.ofNullable(mapToIdentity(resp.getBody()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}