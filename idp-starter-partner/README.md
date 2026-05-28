# IDP :: Starter :: Partner

## Overview

REST partner client factory with configurable timeouts, proxy support, token propagation, and correlation ID forwarding.

## What It Provides

### Core
- `PartnerAutoConfiguration` — Spring Boot auto-configuration entry point
- `IdpPartnerProperties` — `idp.partners.services.*` configuration binding

### Registry
- `PartnerServiceRegistry` — central registry for validated partner configuration access via `get(...)`, `rest(...)`, `soap(...)`, and `has(...)`

### REST Client
- `PartnerWebClientFactory` — builds `WebClient` instances per partner definition
- `PartnerRestException` — typed exception wrapping partner HTTP errors

### Auth
- `OAuthClientCredentialsTokenService` — fetches and caches OAuth 2.0 client-credentials tokens for outbound calls

### Feign Integration
- `PartnerFeignAutoConfiguration` — registers Feign interceptor for all partner Feign clients
- `PartnerFeignRequestInterceptor` — injects correlation ID and bearer token on outbound Feign requests

### Validation
- Startup-time validation of partner configuration (required fields, URL format, auth mode consistency)

## Configuration

```yaml
idp:
  partners:
    services:
      external-echo:
        type: REST
        rest:
          baseUrl: https://api.partner.com
          connectTimeoutMs: 3000
          readTimeoutMs: 10000
          auth:
            mode: PROPAGATE_BEARER   # NONE | PROPAGATE_BEARER | API_KEY | OAUTH
          proxy:
            enabled: false
            host: proxy.corp.com
            port: 8080
```

## Usage

```java
@Autowired PartnerWebClientFactory partnerWebClientFactory;

var client = partnerWebClientFactory.client("external-echo");
var response = client.get()
    .uri("/endpoint")
    .retrieve()
    .bodyToMono(MyResponse.class);
```

## Notes

- Depends on `idp-starter-platform` for correlation ID and token propagation context.
- Consumer services can use either the WebClient-based factory or Feign integration, depending on their preferred client model.

