# IDP :: Test Lib

## Overview

Shared testing annotations, matchers, and helper utilities for IDP consumer services.

## What It Provides

### Included Test Stack
- `spring-boot-starter-test`
- `spring-security-test`
- `json-unit-assertj`
- `awaitility`

### Annotations
- `IdpServiceTest`
- `IdpIntegrationTest`
- `IdpSpringIntegrationTest`
- `IdpPostgresIntegrationTest`
- `IdpWebMvcTest`
- `IdpWebIntegrationTest`

These annotations standardize common Spring test slices and integration-test setup across services.

### Helpers
- `IdpMvcResultMatchers` — reusable MVC assertions
- `IdpTestAuth` — test authentication helpers
- `IdpTestSecurityDisabledConfig` — disables security where needed for focused tests
- `IdpTestCaches` — cache-related test utilities
- `PageAssertions` — pagination assertions

## Typical Usage
Add as a test-scoped dependency in a consumer service:

```xml
<dependency>
  <groupId>io.github.mohankandar.idp</groupId>
  <artifactId>idp-test-lib</artifactId>
  <scope>test</scope>
</dependency>
```

## Notes

- Use this library to keep test slices and helper conventions consistent across services.
- Consumer services can still add service-specific test fixtures on top of these shared utilities.

