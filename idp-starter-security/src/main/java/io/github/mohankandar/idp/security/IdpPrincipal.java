package io.github.mohankandar.idp.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public record IdpPrincipal(
        String applicationName,
        String appToken,
        String firstName,
        String lastName,
        String networkId,
        Set<String> roles,
        Set<String> scopes,
        Map<String, Object> attributes
) {
    public IdpPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static IdpPrincipal fromJwt(Jwt jwt, SecurityProperties props) {
        var c = props.getClaims();
        Map<String, Object> cl = jwt.getClaims();

        String appName = asString(cl.getOrDefault(c.getApplicationName(), ""));
        String appToken = asString(cl.getOrDefault(c.getAppToken(), ""));
        String firstName = asString(cl.getOrDefault(c.getFirstName(), ""));
        String lastName = asString(cl.getOrDefault(c.getLastName(), ""));
        String netId = asString(cl.getOrDefault(c.getNetworkId(), ""));

        Set<String> roles = toSet(cl.get(c.getRoles()));
        Set<String> scopes = toSet(cl.get(c.getScopes()));

        return new IdpPrincipal(appName, appToken, firstName, lastName, netId, roles, scopes, cl);
    }

    private static String asString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> toSet(Object v) {
        if (v == null) {
            return Set.of();
        }
        if (v instanceof String s) {
            if (s.contains(" ")) {
                return new LinkedHashSet<>(Arrays.asList(s.split("\\s+")));
            }
            if (s.contains(",")) {
                return new LinkedHashSet<>(Arrays.asList(s.split(",")));
            }
            return Set.of(s);
        }
        if (v instanceof Collection<?> c) {
            LinkedHashSet<String> out = new LinkedHashSet<>();
            for (Object e : c) {
                out.add(String.valueOf(e));
            }
            return out;
        }
        return Set.of(String.valueOf(v));
    }
}
