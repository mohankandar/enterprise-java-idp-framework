# IDP :: Data JPA

## Overview

Reusable JPA helper types shared by framework modules and consumer services.

## What It Provides

- `Specifications` — reusable criteria helpers such as `eq`, `like`, range, and `between`
- `PageableUtil` — paging convenience helpers aligned with Spring Data
- `SecurityAuditorAware` — audit support that uses `networkId` when security context is available

## Typical Usage

- Pulled in automatically by `idp-starter-data-jpa`
- Can also be added directly when a service wants the helpers without the convenience starter

## Notes

- Does not hard-require Spring Security; it works even when security is absent.
