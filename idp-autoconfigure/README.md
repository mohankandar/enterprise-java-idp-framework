# IDP :: Autoconfigure

## Overview

Spring Boot auto-configuration for common IDP components and shared `idp.*` properties.

## What It Provides

- `IdpAutoConfiguration` — base framework auto-configuration entry point
- `IdpJacksonAutoConfiguration` — shared Jackson/ObjectMapper defaults
- `IdpProperties` — central configuration binding for reusable IDP settings

## Registration

Auto-configurations are registered through:

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## Notes

- This module is intended to be consumed transitively through the IDP starters.
- Keep shared property models and bootstrap wiring here, not in consumer services.
