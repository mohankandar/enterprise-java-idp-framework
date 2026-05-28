# IDP :: Starter :: Security

## Overview

JWT + API Key authentication and outbound token propagation.

## What It Provides

### Filters & Auth
- `ApiKeyAuthFilter` — servlet filter validating `X-API-Key` header against configured accepted keys
- `SecurityAutoConfiguration` — primary `@AutoConfiguration`; wires security filter chain, API key filter, and JWT resource server
- `SecurityProperties` — `idp.security.*` configuration binding (API key enabled/keys, JWT issuer/audience)

### JWT / Authorities
- `DefaultJwtAuthoritiesConverter` — maps JWT claims to Spring Security `GrantedAuthority` list
- `JwtAuthoritiesConverter` — interface; implement to customize claim-to-authority mapping
- `IdpPrincipal` — typed principal record holding `networkId` and resolved authorities from JWT

### Method Security
- `MethodSecurityAutoConfiguration` — enables `@PreAuthorize` / `@PostAuthorize` with IDP's converter wired in

### Token Propagation
- `propagation/` — `TokenPropagationConfig`; registers `RestTemplate` and `WebClient` interceptors that forward the inbound Bearer token to outbound calls

### Web Utilities
- `web/` — security-related `HandlerMethodArgumentResolver`s (e.g., inject `IdpPrincipal` directly into controller method signatures)

### Token Utilities
- `token/` — helpers for extracting raw JWT strings from `SecurityContext`

## Configuration

```yaml
idp:
  security:
    api-key:
      enabled: true
      header: X-API-Key
      accepted-keys: ${API_KEY_SECRET}
    jwt:
      enabled: true
      # Delegates to standard Spring Security OAuth2 resource-server properties:
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://issuer.example.com/oauth2/default
          audiences: my-service
```

## Notes

- Requires `spring-boot-starter-security` and `spring-security-oauth2-resource-server` (pulled in automatically).
- Works with any compliant OIDC/JWT provider.
- API Key and JWT modes can be active simultaneously; API Key filter runs first.
