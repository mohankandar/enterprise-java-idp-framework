package io.github.mohankandar.idp.identity;

import java.util.List;
import java.util.Map;

public record IdpIdentity(
        String networkId,
        String firstName,
        String lastName,
        String email,
        List<String> roles,
        Map<String, Object> attributes
) {
    public IdpIdentity {
        roles = (roles == null) ? List.of() : List.copyOf(roles);
        attributes = (attributes == null) ? Map.of() : Map.copyOf(attributes);
    }

    public static IdpIdentity minimal(String networkId) {
        return new IdpIdentity(networkId, null, null, null, List.of(), Map.of());
    }
}