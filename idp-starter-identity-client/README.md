# IDP :: Starter :: Identity Client

## Overview

Lightweight identity resolution from JWT claims or from an HTTP identity service.

## What It Provides

- `IdpIdentity` — shared identity record used by consumers
- `IdpIdentityClient` — abstraction for resolving the current identity
- `JwtClaimsIdentityClient` — resolves identity directly from JWT claims
- `HttpIdentityClient` — resolves identity from a remote HTTP endpoint
- `IdpIdentityAutoConfiguration` — wires the appropriate client based on configuration

## Configuration

```yaml
idp:
  identity:
    http-enabled: true
    base-url: https://id.company/api
    path-template: /identity/{networkId}
    bearer: ${IDENTITY_SVC_TOKEN:}
```

## Typical Usage

- Use JWT claim resolution when identity data already exists in the token.
- Use HTTP resolution when identity must be enriched from a central identity service.

## Notes

- Keep the JWT-based path as the default when possible to avoid unnecessary network hops.
