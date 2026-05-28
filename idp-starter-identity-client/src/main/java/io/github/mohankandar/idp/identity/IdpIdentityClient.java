package io.github.mohankandar.idp.identity;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface IdpIdentityClient {
    /** Resolve identity for the currently-authenticated user (from SecurityContext). */
    Optional<IdpIdentity> current();

    /** Resolve identity for a specific networkId (may call remote). */
    Optional<IdpIdentity> byNetworkId(@NonNull String networkId, @Nullable String bearerToken);
}
