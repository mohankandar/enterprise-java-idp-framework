# IDP Demo Service

A minimal reference application showcasing how to integrate the **IDP starter modules**:
- `idp-starter-platform` (web, OpenAPI, observability, resilience)
- `idp-starter-security` (API key and JWT resource server support)
- `idp-starter-data` (JPA + Liquibase + Redis, optional embedded Redis)
- `idp-logging-config` (structured JSON logging with masking hooks)
- `idp-data-jpa` (shared persistence utilities)
- `idp-autoconfigure` and `idp-core` (shared primitives and auto-config)

> Target stack: **Java 21**, **Spring Boot 3.2.x**, **H2 (local)**, **Liquibase**, **Redis (embedded for local)**, **springdoc OpenAPI 2.5.x**.

---

## Quick Start

### Prerequisites
- Java **21**
- Maven **3.9+**
- (Optional) Docker, if you want to run external services

### Build
```bash
mvn -q -DskipTests package
```

### Run (local)
```bash
# Profile enables: H2 in-memory DB, Liquibase, embedded Redis, relaxed security for Swagger
mvn -q -pl idp-demo-service -am spring-boot:run -Dspring-boot.run.profiles=local
```
or from IntelliJ **DemoApplication** with `-Dspring.profiles.active=local`

### URLs
- App root: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`  → redirects to `/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator root: `http://localhost:8080/actuator`

> Ensure your security config **permits** Swagger assets in local/dev:
> `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`

---

## Configuration

### Profiles
- **local** (dev-friendly): H2, Liquibase auto-run, optional **embedded Redis**, Swagger open
- **dev/test/prod**: Connect to real DB/Redis; Swagger toggle via properties; strict security

### Key Properties (demo service)
```yaml
# application.yml (demo highlights)

server:
  port: 8080

spring:
  application:
    name: idp-demo-service
  datasource:
    url: jdbc:h2:mem:idp_demo;MODE=LEGACY;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: SA
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none
    open-in-view: false
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yml

# Redis (starter-data)
spring:
  data:
    redis:
      host: localhost
      port: 6379

# Swagger (springdoc)
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true

# Logging (idp-logging-config -> JSON)
logging:
  level:
    root: INFO
    io.github.mohankandar.idp: INFO
    # Quiet common framework chatter (example)
    org.apache.catalina: WARN
    org.apache.coyote: WARN
    org.springframework.boot.web.embedded.tomcat: WARN
    org.springframework.web.servlet.DispatcherServlet: WARN
    org.springframework.boot.web.servlet: WARN
    org.springframework.data.repository.config: WARN
    org.springframework.boot.actuate.endpoint.web: WARN
    org.springframework.security: WARN
    liquibase: WARN
    org.springframework.orm.jpa: WARN
    org.hibernate: WARN
    org.hibernate.orm.deprecation: ERROR
  # Hide banner if desired
spring:
  main:
    banner-mode: off
```

### Security
The demo supports **two auth modes** out-of-the-box; you can enable either or both per route:
- **API Key** via a custom filter (`ApiKeyAuthFilter`) using header like `X-API-Key: <token>`
- **JWT** (resource-server) using `spring-security-oauth2-resource-server`

Typical whitelist for docs in local/dev:
```java
.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
.anyRequest().authenticated();
```

Provide environment or profile properties for:
- `idp.security.api-key.enabled=true`
- `idp.security.api-key.header=X-API-Key`
- `idp.security.api-key.accepted-keys=<comma-separated list>`
- JWT issuer/audience/jwk-set-uri when enabling JWT

---

## Data, DB & Migrations

- **H2 in-memory** is used in `local` for fast boot.
- **Liquibase** runs automatically and applies change sets from `db/changelog`.
- Example change set creates a simple `customer` table and an index.

To inspect H2:
- Add dependency `com.h2database:h2` (already present in demo scope)
- (Optional) Expose H2 console in local if needed

---

## Redis & Caching

- **Embedded Redis** auto-starts in `local` via `idp-starter-data` when enabled.
- For integration environments, set `spring.data.redis.host` and `port` to the shared instance and **disable** embedded.
- Spring Cache can be enabled at the service level using `@EnableCaching` and `@Cacheable` annotations.

Disable **Redis repositories** (recommended unless you model Redis domain entities) and keep JPA as the default repo store:
```properties
spring.data.redis.repositories.enabled=false
```

---

## Endpoints (Sample)

| Method | Path            | Description                |
|-------:|-----------------|----------------------------|
|  GET   | `/actuator/health` | Health check (actuator)   |
|  GET   | `/swagger-ui.html` | Swagger UI                |
|  GET   | `/api/customers`   | Example list endpoint (demo) |
|  POST  | `/api/customers`   | Create a customer (demo)  |

> Business endpoints may require API key or JWT depending on profile/config.

---

## Running with Docker (optional)

Example `Dockerfile` (JAR mode):
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/idp-demo-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Build & run:
```bash
mvn -q -DskipTests package
docker build -t idp-demo-service:local -f idp-demo-service/Dockerfile .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=local idp-demo-service:local
```

---

## Troubleshooting

- **Swagger 401/403**: Verify permit list for `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` and API key/JWT settings.
- **Port conflicts (Redis 6379)**: If another Redis is running, change embedded port or disable embedded and point to external.
- **Liquibase errors**: Confirm `db.changelog-master.yml` location and ensure H2 URL uses `DB_CLOSE_DELAY=-1` for in-memory.
- **Noisy logs**: Adjust `logging.level.*` keys as shown above.

---

## Project Layout (relevant modules)

```
enterprise-java-idp-framework/
├─ idp-core/
├─ idp-autoconfigure/
├─ idp-starter-platform/
├─ idp-starter-security/
├─ idp-starter-data/
├─ idp-data-jpa/
├─ idp-logging-config/
└─ idp-demo-service/   <-- this project
```

---

## Contributing

1. Create a feature branch from `master`
2. Add unit tests where applicable
3. Run `mvn -q -DskipTests=false verify`
4. Submit a PR with a concise summary

---

## License

Copyright © 2025.
This demo is part of the IDP framework. License TBA.
