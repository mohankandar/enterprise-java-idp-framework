# IDP :: Logging Config

## Overview

Central Logback configuration for structured JSON logging with masking support.

## What It Provides

- `MaskingTurboFilter` to scrub SSN, card, token, and password-like values before appenders run
- JSON logging via `logstash-logback-encoder`
- Shared templates: `logback-spring.xml` and `logback.xml`

## Typical Usage

- Added transitively through `idp-starter-platform`
- Can also be included directly when a service wants the IDP logging defaults without the full platform starter

## Notes

- Prefer the shared logback templates unless a service has a strong reason to override them.
- Keep masking behavior centralized here so all services follow the same redaction policy.
