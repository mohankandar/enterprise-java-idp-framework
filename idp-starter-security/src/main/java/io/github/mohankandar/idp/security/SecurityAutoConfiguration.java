package io.github.mohankandar.idp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.security.config.IdpDefaultSecurityPaths;
import io.github.mohankandar.idp.security.config.IdpWebSecurityProperties;
import io.github.mohankandar.idp.security.token.TokenEndpointProperties;
import io.github.mohankandar.idp.security.web.IdpAccessDeniedHandler;
import io.github.mohankandar.idp.security.web.IdpAuthenticationEntryPoint;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfiguration
@EnableConfigurationProperties({IdpWebSecurityProperties.class, SecurityProperties.class})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint idpAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new IdpAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler idpAccessDeniedHandler(ObjectMapper objectMapper) {
        return new IdpAccessDeniedHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain idpSecurityFilterChain(
            HttpSecurity http,
            JwtAuthoritiesConverter authoritiesConverter,
            IdpWebSecurityProperties webProps,
            ObjectProvider<JwtDecoder> jwtDecoderProvider,
            @Qualifier("idpMdcUserEnricherFilter") ObjectProvider<OncePerRequestFilter> mdcFilterProvider,
            ObjectProvider<ApiKeyAuthFilter> apiKeyAuthFilterProvider,
            BearerTokenResolver bearerTokenResolver,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        String[] permitAll = java.util.stream.Stream.concat(
                IdpDefaultSecurityPaths.DEFAULT_PERMIT_ALL.stream(),
                webProps.getPermitPaths().stream()
        ).toArray(String[]::new);

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(h -> h
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(permitAll).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable());

        var jwtConv = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        jwtConv.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        var decoder = jwtDecoderProvider.getIfAvailable();
        if (decoder != null) {
            http.oauth2ResourceServer(oauth -> oauth
                    .bearerTokenResolver(bearerTokenResolver)
                    .jwt(jwt -> jwt
                            .jwtAuthenticationConverter(jwtConv)
                            .decoder(decoder)
                    )
            );
        }

        Filter apiKeyAuthFilter = apiKeyAuthFilterProvider.getIfAvailable();
        if (apiKeyAuthFilter != null) {
            http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        OncePerRequestFilter mdcFilter = mdcFilterProvider.getIfAvailable();
        if (mdcFilter != null) {
            http.addFilterAfter(mdcFilter, BearerTokenAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "false")
    public SecurityFilterChain idpSecurityDisabledFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .oauth2ResourceServer(oauth -> oauth.disable())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public java.util.function.Function<Jwt, IdpPrincipal> idpPrincipalExtractor(SecurityProperties props) {
        return jwt -> IdpPrincipal.fromJwt(jwt, props);
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthoritiesConverter.class)
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthoritiesConverter jwtAuthoritiesConverter(SecurityProperties props) {
        return new DefaultJwtAuthoritiesConverter(props);
    }

    @Bean
    @ConditionalOnBean(TokenEndpointProperties.class)
    @ConditionalOnMissingBean(JwtDecoder.class)
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder idpJwtDecoder(TokenEndpointProperties props, Environment env) {
        boolean production = env.getProperty("idp.framework.production", Boolean.class, false);
        if (production) {
            throw new IllegalStateException(
                    "idp.framework.production=true: HS256/HMAC JWT is not allowed. " +
                            "Configure OIDC issuer/JWKS in a later phase, or set idp.framework.production=false."
            );
        }

        if (!StringUtils.hasText(props.getHmacSecret())) {
            throw new IllegalStateException("idp.security.token-endpoint.hmac-secret must be configured when IDP HS256 decoder is enabled");
        }

        byte[] keyBytes = props.getHmacSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("idp.security.token-endpoint.hmac-secret must be >= 32 bytes");
        }
        var key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    @ConditionalOnMissingBean(BearerTokenResolver.class)
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public BearerTokenResolver idpBearerTokenResolver(IdpWebSecurityProperties webProps) {
        List<RequestMatcher> permitMatchers = webProps.getPermitPaths().stream()
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());

        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

        return request -> {
            for (RequestMatcher m : permitMatchers) {
                if (m.matches(request)) {
                    return null;
                }
            }
            return delegate.resolve(request);
        };
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class ApiKeySecurityConfiguration {

        @Bean
        @ConditionalOnMissingBean(ApiKeyAuthFilter.class)
        @ConditionalOnProperty(prefix = "idp.security.api-key", name = "enabled", havingValue = "true")
        public ApiKeyAuthFilter apiKeyAuthFilter(SecurityProperties props) {
            var f = new ApiKeyAuthFilter(props);
            f.setAuthenticationManager(authentication -> {
                var pre = (org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken) authentication;
                var authorities = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_API");
                var authed = new org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken(
                        pre.getPrincipal(), pre.getCredentials(), authorities);
                authed.setDetails(pre.getDetails());
                return authed;
            });
            return f;
        }
    }
}