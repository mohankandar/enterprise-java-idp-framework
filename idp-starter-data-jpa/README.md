# IDP :: Starter :: Data JPA

## Overview

Backward-compatible convenience starter that brings together IDP data defaults with Spring Data JPA.

## What It Provides
- `idp-starter-data` — Liquibase, Redis/cache helpers, JDBC/data defaults
- `spring-boot-starter-data-jpa` — JPA repositories + Hibernate integration
- `idp-data-jpa` — shared JPA helpers such as specifications, paging helpers, and auditing support

## Typical Usage

Use this starter when your service needs relational persistence with JPA and you want the IDP data conventions in one dependency.

```xml
<dependency>
  <groupId>io.github.mohankandar.idp</groupId>
  <artifactId>idp-starter-data-jpa</artifactId>
</dependency>
```

## Notes

- This module exists mainly as a compatibility/convenience layer.
- If you want finer control, depend on `idp-starter-data`, `spring-boot-starter-data-jpa`, and `idp-data-jpa` individually.

