package io.github.mohankandar.idp.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for DefaultJwtAuthoritiesConverter.
 * Validates JWT claim to GrantedAuthority mapping for roles and scopes.
 */
class DefaultJwtAuthoritiesConverterTest {

    private final SecurityProperties props = new SecurityProperties();
    private final DefaultJwtAuthoritiesConverter converter = new DefaultJwtAuthoritiesConverter(props);

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }

    @Test
    void convertsGroupsToRoleAuthorities() {
        Jwt jwt = buildJwt(Map.of("groups", List.of("admin", "user")));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void convertsRolesToRoleAuthorities() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("editor")));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_EDITOR");
    }

    @Test
    void convertsScopesToScopeAuthorities() {
        Jwt jwt = buildJwt(Map.of("scope", "read write"));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
            .contains("SCOPE_READ", "SCOPE_WRITE");
    }

    @Test
    void convertsScopesFromScpArray() {
        Jwt jwt = buildJwt(Map.of("scp", List.of("openid", "profile")));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
            .contains("SCOPE_OPENID", "SCOPE_PROFILE");
    }

    @Test
    void normalizesRolesToUpperCase() {
        Jwt jwt = buildJwt(Map.of("groups", List.of("idp-admin-role")));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Normalizer uppercases and keeps dash (pattern: [^A-Za-z0-9:_.-] -> _)
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_IDP-ADMIN-ROLE");
    }

    @Test
    void deduplicatesAuthorities() {
        // groups and roles both have "admin"
        Jwt jwt = buildJwt(Map.of("groups", List.of("admin"), "roles", List.of("admin")));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        long adminCount = authorities.stream()
            .filter(a -> a.getAuthority().equals("ROLE_ADMIN"))
            .count();
        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    void returnsEmptyWhenNoRelevantClaims() {
        Jwt jwt = buildJwt(Map.of("sub", "user123"));
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).isEmpty();
    }
}

