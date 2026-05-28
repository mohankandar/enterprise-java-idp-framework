package io.github.mohankandar.idp.security.token;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Additional safety guard for the token endpoint.
 * <p>
 * The endpoint will only be created when the active Spring profiles intersect with
 * the configured {@code idp.security.token-endpoint.allowed-profiles} list.
 * <p>
 * Default allowed profiles are {@code local,dev}.
 */
public class TokenEndpointProfileCondition implements Condition {

  private static final String ALLOWED_PROFILES_PROPERTY =
      "idp.security.token-endpoint.allowed-profiles";

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Environment env = context.getEnvironment();

    String raw = env.getProperty(ALLOWED_PROFILES_PROPERTY, "local,dev");
    Set<String> allowed = Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

    if (allowed.isEmpty()) {
      // No allowed profiles configured → fail closed (do not expose endpoint)
      return false;
    }

    String[] activeProfiles = env.getActiveProfiles();
    if (activeProfiles == null || activeProfiles.length == 0) {
      // No active profiles → also fail closed
      return false;
    }

    String[] allowedArray = allowed.toArray(new String[0]);
    Profiles profiles = Profiles.of(allowedArray);

    // Only match when at least one allowed profile is active
    return env.acceptsProfiles(profiles);
  }
}
