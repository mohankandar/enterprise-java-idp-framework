# IDP :: Starter :: Platform

## Overview

Web/Actuator defaults, correlation ID filter, global exception handler, structured logging enrichment, async MDC propagation, OpenAPI security configuration, and performance instrumentation.

## What It Provides

### 🌐 Web & HTTP
- `GlobalExceptionHandler` — unified `@RestControllerAdvice` mapping exceptions to IDP error envelopes
- `IdpWebErrorAutoConfiguration` — wires `GlobalExceptionHandler`
- `RestTemplateConfig` — pre-configured `RestTemplate` bean with IDP defaults
- `WebClientConfig` — pre-configured reactive `WebClient` bean
- `IdpHttpProperties` — configurable connect/read timeouts for outbound HTTP clients

### 🔗 Feign Client Support
- Feign auto-configuration included via `spring-cloud-starter-openfeign`
- Correlation ID and token propagation applied automatically to Feign requests

### 📋 Logging & Correlation
- `CorrelationIdFilter` — reads/generates `X-Correlation-Id` and sets it in MDC
- `LogHeadersFilter` — logs selected inbound request headers
- `MdcUserEnricherFilter` — enriches MDC with authenticated user identity
- `MaskingMessageConverter` / `MaskingTurboFilter` — masks sensitive data (SSN, CCN, tokens, passwords) in log output
- `LoggingContextValues` — shared MDC key constants

### ⚡ Async MDC Propagation
- `AsyncConfig` — configures Spring `@Async` thread pools
- `MdcTaskDecorator` — propagates MDC context to async threads so correlation IDs are preserved

### 📊 Performance Instrumentation
- `IdpLayerPerformanceAspect` — AOP aspect timing service/repository layer calls
- `IdpPerformanceLogger` — logs performance metrics in structured JSON
- `IdpPerformanceMetrics` — Micrometer-based metric recording
- `IdpRestTemplatePerformanceCustomizer` — adds timing interceptor to `RestTemplate`
- `IdpWebClientPerformanceFilter` — adds timing filter to reactive `WebClient`
- `IdpPerformanceProperties` — toggle and threshold configuration

### 🔒 OpenAPI / Swagger Security
- `IdpOpenApiSecurityAutoConfiguration` — registers security scheme definitions for Swagger UI
- `IdpOpenApiProperties` — `idp.openapi.*` properties for API title/version/security scheme config

### 🛠 Auto-Configuration Entry Point
- `PlatformAutoConfiguration` — top-level `@AutoConfiguration` that imports all sub-configurations above

## Key Dependencies Pulled In
- `spring-boot-starter-web`
- `spring-boot-starter-actuator`
- `spring-boot-starter-validation`
- `spring-boot-starter-aop`
- `spring-cloud-starter-openfeign`
- `springdoc-openapi-starter-webmvc-ui`
- `idp-core`, `idp-autoconfigure`, `idp-logging-config`, `idp-starter-security`

## Configuration

```yaml
idp:
  http:
    connectTimeoutMs: 3000
    readTimeoutMs: 10000

  openapi:
    title: My Service API
    version: v1
    security-scheme: apiKey   # or bearer

  performance:
    enabled: true
    slow-threshold-ms: 500

logging:
  level:
    root: INFO
    io.github.mohankandar.idp: INFO
```

## Typical Usage

Add this single starter to your consumer service POM — it brings web, security, logging, and observability in one pull:

```xml
<dependency>
  <groupId>io.github.mohankandar.idp</groupId>
  <artifactId>idp-starter-platform</artifactId>
</dependency>
```

