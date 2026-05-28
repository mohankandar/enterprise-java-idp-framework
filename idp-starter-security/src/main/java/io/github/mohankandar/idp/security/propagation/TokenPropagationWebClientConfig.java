package io.github.mohankandar.idp.security.propagation;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@ConditionalOnClass({WebClient.class, ExchangeFilterFunction.class})
public class TokenPropagationWebClientConfig {

    @Bean
    public ExchangeFilterFunction bearerPropagationFilter() {
        return (request, next) -> {
            var ctx  = SecurityContextHolder.getContext();
            var auth = (ctx != null) ? ctx.getAuthentication() : null;

            final String token = resolveBearerToken(auth);

            ClientRequest mutated = (token != null && !token.isBlank())
                ? ClientRequest.from(request)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build()
                : request;

            return next.exchange(mutated);
        };
    }

    private static String resolveBearerToken(org.springframework.security.core.Authentication auth) {
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jat) {
            return jat.getToken().getTokenValue();
        }
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication bta) {
            return bta.getToken().getTokenValue();
        }
        if (auth != null && auth.getCredentials() instanceof String s && !s.isBlank()) {
            return s; // fallback (e.g., API key)
        }
        return null;
    }
}
