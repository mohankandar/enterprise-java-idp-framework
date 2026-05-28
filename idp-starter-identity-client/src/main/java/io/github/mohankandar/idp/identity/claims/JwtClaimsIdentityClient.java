package io.github.mohankandar.idp.identity.claims;

import io.github.mohankandar.idp.identity.IdpIdentity;
import io.github.mohankandar.idp.identity.IdpIdentityClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

public class JwtClaimsIdentityClient implements IdpIdentityClient {

    @Override
    public Optional<IdpIdentity> current() {
        var ctx = SecurityContextHolder.getContext();
        if (ctx == null) return Optional.empty();
        return fromAuthentication(ctx.getAuthentication());
    }

    @Override
    public Optional<IdpIdentity> byNetworkId(String networkId, String bearerToken) {
        // claims-only client cannot look up arbitrary networkId
        return Optional.empty();
    }

    private Optional<IdpIdentity> fromAuthentication(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Map<String,Object> claims = jwt.getClaims();

            String networkId = firstNonBlank(
                    claimAsString(claims,"networkId"),
                    claimAsString(claims,"preferred_username"),
                    claimAsString(claims,"uid"),
                    auth.getName());

            String firstName = claimAsString(claims,"given_name");
            String lastName  = claimAsString(claims,"family_name");
            String email     = claimAsString(claims,"email");

            List<String> roles = extractRoles(claims, jwtAuth.getAuthorities());

            return Optional.of(new IdpIdentity(networkId, firstName, lastName, email, roles, claims));
        }
        // Support API-key auth (PreAuthenticatedAuthenticationToken) and other
        // non-JWT authentications by falling back to the authenticated principal.
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            String networkId = String.valueOf(auth.getPrincipal());
            if (networkId != null && !networkId.isBlank() && !"anonymousUser".equalsIgnoreCase(networkId)) {
                List<String> roles = auth.getAuthorities() == null
                    ? List.of()
                    : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
                return Optional.of(new IdpIdentity(networkId, null, null, null, roles, Map.of()));
            }
        }
        return Optional.empty();
    }

    private static String claimAsString(Map<String,Object> claims, String key) {
        Object v = claims.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static List<String> extractRoles(Map<String,Object> claims, Collection<? extends GrantedAuthority> auths) {
        // from authorities (scope/ROLE_*)
        Set<String> roles = auths == null ? new HashSet<>() :
                auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        // from custom claim "roles"
        Object c = claims.get("roles");
        if (c instanceof Collection<?> col) {
            col.forEach(x -> roles.add(String.valueOf(x)));
        }
        return List.copyOf(roles);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
