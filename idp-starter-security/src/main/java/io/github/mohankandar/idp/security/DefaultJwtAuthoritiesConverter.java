package io.github.mohankandar.idp.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default converter: maps claims to authorities. - "groups" or "roles" -> ROLE_* - "scp" (array) or
 * "scope" (space-delimited) -> SCOPE_*
 */
public class DefaultJwtAuthoritiesConverter implements JwtAuthoritiesConverter {

  private final SecurityProperties props;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "SecurityProperties is a framework configuration holder intentionally retained for claim resolution."
    )
  public DefaultJwtAuthoritiesConverter(SecurityProperties props) {
    this.props = props;
  }

  private static String normalize(String s) {
      return s.trim().replaceAll("[^A-Za-z0-9:_.-]", "_").toUpperCase(Locale.ROOT);
  }

  private static List<String> readStringArray(Object claim) {
      if (claim == null) {
          return List.of();
      }
      if (claim instanceof Collection<?> col) {
          return col.stream().map(Object::toString).toList();
      }
      if (claim.getClass().isArray()) {
          return Arrays.stream((Object[]) claim).map(Object::toString).toList();
      }
    return List.of(claim.toString());
  }

  private static List<String> readScopes(Map<String, Object> claims) {
      if (claims == null) {
          return List.of();
      }
    Object scp = claims.get("scp");
      if (scp instanceof Collection<?> col) {
          return col.stream().map(Object::toString).toList();
      }
    Object scope = claims.get("scope");
      if (scope instanceof String s) {
          return Arrays.asList(s.split("\\s+"));
      }
    return List.of();
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Object> claims = jwt.getClaims();
    var c = props.getClaims();

    List<String> roleClaims = new ArrayList<>();
      roleClaims.addAll(readStringArray(claims.get("groups")));
    roleClaims.addAll(readStringArray(claims.get("roles")));
    roleClaims.addAll(readStringArray(claims.get(c.getRoles())));

    List<String> scopeClaims = new ArrayList<>();
      scopeClaims.addAll(readScopes(claims));
    scopeClaims.addAll(readStringArray(claims.get(c.getScopes())));

    Set<String> out = new LinkedHashSet<>();
    roleClaims.forEach(r -> out.add("ROLE_" + normalize(r)));
    scopeClaims.forEach(s -> out.add("SCOPE_" + normalize(s)));
    return out.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());
  }
}
