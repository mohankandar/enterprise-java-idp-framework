# IDP :: Starter :: Data

## Overview

Shared data-layer defaults for JDBC, Liquibase, Redis, and cache auto-configuration.

## What It Provides

- Datasource + Liquibase wiring
- Optional embedded Redis for `local`
- `JdbcTemplate` helper

## Caching (IDP Cache v1)

IDP provides an opt-in, production-safe caching default:

- Enable with `idp.cache.enabled=true`
- Uses Redis-backed `CacheManager` when Redis is reachable
- If Redis is unreachable, the service can still start (see `fail-fast`) and cache operations can be configured to fail-open
- Optional fail-fast mode: `idp.cache.fail-fast=true`
- Fail-open cache operations (default): `idp.cache.fail-open=true`
- Default TTL and per-cache TTL overrides

### Example: local with embedded Redis

```yml
idp:
  data:
    redisEmbedded:
      enabled: true
      port: 6379
      onlyWhenLocalProfile: true

spring:
  data:
    redis:
      host: localhost
      port: 6379

idp:
  cache:
    enabled: true
    default-ttl: 10m
    ttl:
      sites: 2m
      sitesByUser: 30s
    key-prefix: idp-sales-site-service
```

### Cache key helper

Use `IdpCacheKeys` to build stable keys (supports Pageable/Sort):

```java
@Cacheable(cacheNames = "sites",
  key = "T(io.github.mohankandar.idp.data.cache.IdpCacheKeys).of('sites', #active, #country, #q, #pageable)")
public Page<SiteView> getSites(Boolean active, String country, String q, Pageable pageable) {
  ...
}
```

### Health indicator

If the consumer app includes Spring Boot Actuator, enabling IDP cache registers a `idpCache` health component
showing whether Redis is reachable and which CacheManager is active.

## Typical Usage

Add this starter when a service needs the IDP data conventions without the full JPA convenience starter.

## Notes

- Spring Data JDBC / Spring Data Redis (optional)
- `idp-core`, `idp-autoconfigure`

> **Note:** `idp-data-jpa` is a sibling module (JPA helper types). Use `idp-starter-data-jpa` if you need JPA — it combines `idp-starter-data` + Spring Data JPA + `idp-data-jpa` in one pull.
