# IDP :: Starter :: SOAP Partner

## Overview

Apache CXF-based SOAP partner client starter with timeout configuration, proxy support, correlation ID propagation, and fault handling.

## What It Provides

### Auto-Configuration
- `IdpSoapPartnerAutoConfiguration` — Spring Boot auto-configuration entry point; wires `SoapClientFactory` and interceptors

### Client Factory
- `SoapClientFactory` — creates CXF JAX-WS proxy clients bound to `idp.partners.services.*` configuration
- CXF engine internally configured (no standalone CXF servlet required)

### Interceptors
- `CorrelationIdSoapOutInterceptor` — CXF outbound interceptor that injects the current `X-Correlation-Id` into SOAP headers

### Fault Handling
- Fault-to-exception mapping utilities for clean error propagation from SOAP faults to typed exceptions

## Configuration

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
            host: proxy.corp.com
            port: 8080
```

## Usage

Generate your WSDL → Java bindings via `cxf-codegen-plugin` in your app POM, then:

```java
@Autowired SoapClientFactory soapClientFactory;

HelloService client = soapClientFactory.createClient("hello-service", HelloService.class);
HelloResponse resp = client.sayHello(new HelloRequest("World"));
```

## Notes

- Does not start a CXF servlet; bus is embedded/local only.
- WSDL-to-Java code generation must be done in the consumer service POM.
- Correlation ID is automatically forwarded in the SOAP `<S:Header>` block.
- Depends on Apache CXF and `idp-starter-platform` for runtime client behavior and correlation context.

