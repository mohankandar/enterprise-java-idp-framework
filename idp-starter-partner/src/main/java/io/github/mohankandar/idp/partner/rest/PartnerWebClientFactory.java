package io.github.mohankandar.idp.partner.rest;

import io.github.mohankandar.idp.partner.auth.OAuthClientCredentialsTokenService;
import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import io.netty.channel.ChannelOption;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

/**
 * Creates partner-configured WebClient instances by partner name.
 */
public class PartnerWebClientFactory {

    private final PartnerServiceRegistry registry;
    private final WebClient.Builder baseBuilder;
    private final OAuthClientCredentialsTokenService tokenService;

    public PartnerWebClientFactory(PartnerServiceRegistry registry, WebClient.Builder baseBuilder, OAuthClientCredentialsTokenService tokenService) {
        this.registry = registry;
        this.baseBuilder = baseBuilder;
        this.tokenService = tokenService;
    }

    public WebClient client(String partnerName) {
        IdpPartnerProperties.RestProperties props = registry.rest(partnerName);

        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, safeMs(props.getConnectTimeoutMs(), 1000))
            .responseTimeout(Duration.ofMillis(safeMs(props.getReadTimeoutMs(), 5000)));

        WebClient.Builder b = baseBuilder.clone()
            .baseUrl(props.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient));

        Map<String, String> headers = props.getHeaders();
        if (!headers.isEmpty()) {
            b.defaultHeaders(h -> headers.forEach(h::add));
        }

        // Auth
        IdpPartnerProperties.RestAuthProperties auth = props.getAuth();
        IdpPartnerProperties.RestAuthMode mode =
            (auth != null && auth.getMode() != null) ? auth.getMode() : IdpPartnerProperties.RestAuthMode.PROPAGATE_BEARER;

        if (mode == IdpPartnerProperties.RestAuthMode.API_KEY) {
            String header = (auth != null) ? auth.getApiKeyHeader() : null;
            String value = (auth != null) ? auth.getApiKeyValue() : null;
            if (StringUtils.hasText(header) && StringUtils.hasText(value)) {
                b.defaultHeader(header, value);
            }
        } else if (mode == IdpPartnerProperties.RestAuthMode.PROPAGATE_BEARER) {
            b.filter(bearerPropagationFilter());
        } else if (mode == IdpPartnerProperties.RestAuthMode.OAUTH) {
            b.filter((request, next) -> next.exchange(
                ClientRequest.from(request)
                    .headers(h -> h.setBearerAuth(tokenService.getBearerToken(partnerName)))
                    .build()
            ));
        }

        // Error mapping filter (basic)
        b.filter((request, next) -> next.exchange(request).flatMap(resp -> {
            if (resp.statusCode().isError()) {
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> reactor.core.publisher.Mono.error(
                        new PartnerRestException(
                            partnerName,
                            request.method().name(),
                            request.url().toString(),
                            resp.statusCode().value(),
                            "REST partner '" + partnerName + "' returned HTTP " + resp.statusCode().value()
                                + (body.isBlank() ? "" : " (body truncated)"),
                            null
                        )
                    ));
            }
            return reactor.core.publisher.Mono.just(resp);
        }));

        return b.build();
    }

    private static int safeMs(Integer v, int dflt) {
        return (v != null && v > 0) ? v : dflt;
    }

    /**
     * Token propagation without requiring oauth2 classes at compile time.
     * If Spring Security is not on the classpath or no token is present, this is a no-op.
     */
    private static ExchangeFilterFunction bearerPropagationFilter() {
        return (request, next) -> {
            String token = resolveBearerToken();
            ClientRequest mutated = (token != null && !token.isBlank())
                ? ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build()
                : request;
            return next.exchange(mutated);
        };
    }

    private static String resolveBearerToken() {
        try {
            Class<?> sch = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Method getContext = sch.getMethod("getContext");
            Object ctx = getContext.invoke(null);
            if (ctx == null) return null;
            Method getAuth = ctx.getClass().getMethod("getAuthentication");
            Object auth = getAuth.invoke(ctx);
            if (auth == null) return null;

            // If auth is JwtAuthenticationToken or BearerTokenAuthentication, try token extraction reflectively:
            String cn = auth.getClass().getName();

            if (cn.equals("org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken")) {
                Method getToken = auth.getClass().getMethod("getToken");
                Object jwt = getToken.invoke(auth);
                if (jwt != null) {
                    Method getTokenValue = jwt.getClass().getMethod("getTokenValue");
                    Object v = getTokenValue.invoke(jwt);
                    return (v instanceof String s) ? s : null;
                }
            }

            if (cn.equals("org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication")) {
                Method getToken = auth.getClass().getMethod("getToken");
                Object token = getToken.invoke(auth);
                if (token != null) {
                    Method getTokenValue = token.getClass().getMethod("getTokenValue");
                    Object v = getTokenValue.invoke(token);
                    return (v instanceof String s) ? s : null;
                }
            }

            // Fallback: credentials string
            Method getCreds = auth.getClass().getMethod("getCredentials");
            Object creds = getCreds.invoke(auth);
            return (creds instanceof String s && !s.isBlank()) ? s : null;

        } catch (ClassNotFoundException e) {
            return null; // Spring Security not present
        } catch (Exception e) {
            return null; // don't fail partner calls due to propagation issues
        }
    }
}
