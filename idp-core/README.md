# IDP :: Core

## Overview

Core utilities and abstractions used by all IDP modules and consumer services. No Spring Boot auto-configuration here — just shared models and helpers.

## What It Provides

### `api/`
- `ApiResponse<T>` — standard response envelope wrapping data or errors
- `ErrorCode` / `IdpErrorCode` — enum-based error code contract
- `ErrorDetail` — structured error detail (code, message, field)

### `error/`
- Base exception types (e.g., `IdpException`, business vs. system error split)

### `paging/`
- Paging request/response models aligned with Spring Data `Pageable`

### `logging/`
- Masking utility interfaces and patterns (SSN, CCN, token, password)

### `util/`
- Common helpers: time, collections, string utilities

## Notes

- Minimal — Spring Context only (no web, no security, no data)
