package io.github.mohankandar.idp.security.token;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping(path = "/api/token", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
    name = "Token",
    description = "Generate a short-lived JWT for manual testing",
    extensions = @Extension(properties = @ExtensionProperty(name = "x-order", value = "0"))
)
public class TokenEndpointController {

  private static final Set<String> RESERVED = Set.of(
          "iss", "sub", "iat", "nbf", "exp", "jti", "aud", "applicationID", "userID", "roles"
  );

  private final TokenEndpointProperties props;
  private final IdpTokenProperties tokenProps;
  private final SecretKey key;

  @SuppressFBWarnings(
          value = {"EI_EXPOSE_REP2", "CT_CONSTRUCTOR_THROW"},
          justification = "Spring configuration holders are intentionally retained for endpoint evaluation."
  )
  public TokenEndpointController(TokenEndpointProperties props, IdpTokenProperties tokenProps) {
    this.key = buildSigningKey(props.getHmacSecret());
    this.props = props;
    this.tokenProps = tokenProps;
  }

  private static SecretKey buildSigningKey(String hmacSecret) {
    byte[] bytes = hmacSecret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("idp.security.token-endpoint.hmac-secret must be >= 32 bytes");
    }
    return Keys.hmacShaKeyFor(bytes);
  }

  @Operation(summary = "Generate a short-lived JWT for manual testing")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Required: userId. Optional: aud, roles.",
      required = true,
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "Minimal",
                  value = "{\n  \"userId\": \"user123\"\n}"
              ),
              @ExampleObject(
                  name = "With roles and audience",
                      value = "{\n  \"userId\": \"user123\",\n  \"aud\": \"idp-demo-client\",\n  \"roles\": [\"ROLE_USER\", \"ROLE_ADMIN\"]\n}"
              )
          }
      )
  )
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> issue(@RequestBody TokenRequest req, HttpServletRequest http) {
    String subject = org.springframework.util.StringUtils.hasText(req.userId) ? req.userId : "user";
    if (!props.isEnabled()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ErrorBody("TOKEN_ISSUANCE_DISABLED", "Token issuance is disabled in this environment."));
    }

    var now = Instant.now();
    var exp = now.plusSeconds(props.getExpirySeconds());

    var roles = (req.roles == null || req.roles.isEmpty()) ? List.of("ROLE_USER") : req.roles;

    var jwtBuilder = Jwts.builder()
        .setIssuer(props.getIssuer())
        .setSubject(subject)
        .setIssuedAt(Date.from(now))
        .setNotBefore(Date.from(now))
        .setExpiration(Date.from(exp))
        .setId(UUID.randomUUID().toString())
        .claim("applicationID", props.getIssuer())
        .claim("userID", subject)
        .claim("roles", roles);

    if (StringUtils.hasText(req.aud)) {
      jwtBuilder.setAudience(req.aud);
    }
    if (StringUtils.hasText(props.getKid())) {
      jwtBuilder.setHeaderParam("kid", props.getKid());
    }

    if (tokenProps != null && tokenProps.getClaimsDefaults() != null) {
      for (Map.Entry<String, Object> e : tokenProps.getClaimsDefaults().entrySet()) {
        String k = e.getKey();
        if (StringUtils.hasText(k) && !RESERVED.contains(k)) {
          jwtBuilder.claim(k, e.getValue());
        }
      }
    }

    String token = jwtBuilder.signWith(key, SignatureAlgorithm.HS256).compact();

    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header("Pragma", "no-cache")
        .contentType(MediaType.TEXT_PLAIN)
        .body(token);
  }

  @Schema(
      name = "TokenRequest",
      description = "Provide your userId. applicationID is derived from server config (issuer).",
      requiredProperties = {"userId"}
  )
  public static class TokenRequest {
    @Schema(description = "User identifier (subject). Also stamped as the `userID` claim.", example = "user123")
    @NotBlank
    @JsonAlias({"sub", "userID"})
    public String userId;

    @Schema(description = "Audience claim (optional).", example = "idp-demo-client")
    public String aud;

    @Schema(
            description = "Authorities in Spring style (optional). Defaults to ROLE_USER.",
            example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]"
    )
    public List<String> roles;
  }

  public static class TokenResponse {
    public final String token;
    public TokenResponse(@JsonProperty("token") String token) {
      this.token = token;
    }
  }

  public static class ErrorBody {
    public final String code;
    public final String message;
    public ErrorBody(String code, String message) {
      this.code = code;
      this.message = message;
    }
  }
}
