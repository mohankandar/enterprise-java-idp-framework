# Enterprise Java IDP Framework

IDP is a modular Spring Boot framework for building enterprise APIs with consistent defaults for web, security, data, logging, and testing.

The goal is simple: teams spend less time on platform plumbing and more time on business features.

## What You Get

- **Opinionated Spring Boot starters** with override-friendly configuration
- **Consistent API behavior** (error envelope, correlation, logging context)
- **Security defaults** (API key and JWT resource server support)
- **Data defaults** (Liquibase, Redis helpers, cache auto-configuration)
- **Structured logging** (JSON layout, automatic masking, correlation ID propagation)
- **Partner integrations** (REST and SOAP client helpers with timeout/proxy/TLS controls)
- **Shared quality gates** (tests, coverage, static analysis via parent build)

## Modules In This Repository

| Module | Purpose |
| --- | --- |
| `idp-core` | Core API models, errors, paging, masking, and shared utilities |
| `idp-autoconfigure` | Base auto-configuration and common `idp.*` properties |
| `idp-logging-config` | Central Logback JSON layout and masking conventions |
| `idp-data-jpa` | JPA helper types and shared persistence support |
| `idp-starter-data` | JDBC/Liquibase/Redis utilities and cache auto-configuration |
| `idp-starter-data-jpa` | Convenience starter: `idp-starter-data` + Spring Data JPA |
| `idp-starter-security` | API key and JWT security auto-configuration + token propagation |
| `idp-starter-platform` | Web defaults, correlation, global exception handling, OpenAPI support, async MDC propagation, performance instrumentation |
| `idp-starter-identity-client` | Identity resolution from JWT and/or HTTP identity endpoint |
| `idp-starter-partner` | Partner client configuration and REST partner client helpers |
| `idp-starter-soap-partner` | CXF-based SOAP partner client starter |
| `idp-test-lib` | Shared test annotations/utilities for IDP consumer services |
| `idp-build-config` | Shared static analysis resources (Checkstyle/SpotBugs configs) |
| `idp-bom` | Centralized dependency version management |
| `idp-parent` | Shared Maven plugin and quality gate configuration |
| `idp-demo-service` | Reference consumer app that exercises the framework end to end |

## Quick Start

### Prerequisites

- Java 21
- Maven 3.9+

### Build the Framework

```bash
mvn clean install -DskipTests
```

### Run the Demo Service

```bash
mvn -pl idp-demo-service -am spring-boot:run -Dspring-boot.run.profiles=local
```

Useful local URLs:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/actuator/health`

For demo-specific details, see `idp-demo-service/README.md`.

## Create a Consumer Service

Use `idp-parent` as parent (or import `idp-bom` if you have your own parent), then add the starters you need.

```xml
<parent>
  <groupId>io.github.mohankandar.idp</groupId>
  <artifactId>idp-parent</artifactId>
  <version>0.1.2-SNAPSHOT</version>
</parent>

<dependencies>
  <dependency>
    <groupId>io.github.mohankandar.idp</groupId>
    <artifactId>idp-starter-platform</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.mohankandar.idp</groupId>
    <artifactId>idp-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.mohankandar.idp</groupId>
    <artifactId>idp-starter-data-jpa</artifactId>
  </dependency>
</dependencies>
```

Then configure your environment-specific properties (`application-local.yml`, `application-dev.yml`, etc.) for DB, Redis, and security settings.

## Testing and Quality Gates

IDP standardizes test and quality behavior through `idp-parent` and `idp-test-lib`:

- Unit test defaults via Surefire
- Integration test lane via Failsafe (`*IT.java` and `it` tag)
- Coverage enforcement controls (JaCoCo)
- Static analysis hooks (Checkstyle, SpotBugs)

Consumer services can turn policies on/off using properties in their own `pom.xml` (for example, coverage minimums, `skipITs`, and static analysis flags).

## Code Style and Static Analysis

IDP supports static quality checks through parent-managed plugin configuration and shared rules in `idp-build-config`.

- Checkstyle for code style rules
- SpotBugs for bytecode-level defect detection

Enable these checks in a consumer app by setting:

```xml
<properties>
  <static.analysis.enabled>true</static.analysis.enabled>
</properties>
```

Then run:

```bash
mvn clean verify -Dstatic.analysis.enabled=true
```

## Key Capabilities

### 📜 Structured Logging

IDP provides **central JSON logging** with automatic masking for sensitive data (SSNs, credit cards, tokens, passwords).

Every request automatically includes:
- `X-Correlation-Id` in headers and MDC
- `@timestamp` (ISO 8601)
- `level`, `logger`, `thread`, `message`
- Application context (`app`, `appVersion`)
- Masked sensitive fields in request/response bodies

**Enable masking:**
```yaml
idp:
  logging:
    masking-enabled: true

logging:
  level:
    root: INFO
    io.github.mohankandar.idp: INFO
```

See `idp-logging-config/` and `idp-starter-platform/` for MaskingTurboFilter and MaskingMessageConverter.

### 🔗 REST Partner Integration

`idp-starter-partner` provides a unified REST partner client factory with:
- Configurable timeouts (connect, read)
- Optional HTTP proxy support
- SSL/TLS certificate validation
- Automatic outbound token propagation (JWT from inbound request)
- Correlation ID forwarding

**Example configuration:**
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
            mode: PROPAGATE_BEARER  # or NONE, API_KEY, OAUTH
          proxy:
            enabled: false
            host: proxy.corp.com
            port: 8080
```

Then inject `PartnerWebClientFactory` and call:
```java
var client = partnerWebClientFactory.client("external-echo");
var response = client.get()
    .uri("/endpoint")
    .retrieve()
    .bodyToMono(MyResponse.class);
```

### 📡 SOAP Partner Integration

`idp-starter-soap-partner` (built on Apache CXF) provides SOAP client configuration with:
- Automatic WSDL → Java binding via Maven code gen
- Connection and read timeouts
- HTTP proxy support
- Correlation ID propagation to SOAP headers
- Fault handling and error mapping

**Example configuration:**
```yaml
idp:
  partners:
    services:
      hello-service:
        type: SOAP
        soap:
          endpoint: https://webservices.partner.com/hello
          connectTimeoutMs: 3000
          readTimeoutMs: 15000
          proxy:
            enabled: false
```

**Generated SOAP client usage:**
```java
var soapClient = soapClientFactory.createClient("hello-service", HelloService.class);
HelloResponse resp = soapClient.sayHello(new HelloRequest("World"));
```

---

## Performance Instrumentation and Ownership

Framework repository owns reusable performance-test conventions and helpers (runner, stats model, assertion utilities, gate guidance).

Consumer service repositories own service-specific perf lanes:

- `application-perf.yml`
- JMeter plans and input datasets
- deterministic seed data
- endpoint-specific thresholds

Recommended practice: use JMeter as local/scheduled service validation, not as the default fast PR gate.

## Repository Layout

```text
enterprise-java-idp-framework/
  idp-bom/
  idp-build-config/
  idp-parent/
  idp-core/
  idp-autoconfigure/
  idp-logging-config/
  idp-data-jpa/
  idp-starter-data/
  idp-starter-data-jpa/
  idp-starter-identity-client/
  idp-starter-security/
  idp-starter-platform/
  idp-starter-partner/
  idp-starter-soap-partner/
  idp-test-lib/
  idp-demo-service/
```

## Contribution Notes

- Keep module boundaries clear; avoid cross-starter coupling unless intentional
- Prefer auto-configuration + properties over app-specific hardcoding
- Add or update tests when behavior changes
- Run a full validation build before opening a PR

```bash
mvn clean verify
```
