# IDP :: Parent

## Overview

Parent POM for the IDP framework and consumer services. It provides shared build configuration, plugin management, and quality-gate defaults.

## What It Provides

- Java 21, UTF-8, and reproducible build defaults
- Dependency version alignment through `idp-bom`
- Common Maven plugin management (compiler, surefire, failsafe, jar, JaCoCo, static analysis)

## Typical Usage

```xml
<parent>
  <groupId>io.github.mohankandar.idp</groupId>
  <artifactId>idp-parent</artifactId>
  <version>0.1.2-SNAPSHOT</version>
</parent>
```

## Notes

- Prefer `idp-parent` for consumer services unless an existing corporate parent must remain in place.
- When you cannot inherit from `idp-parent`, import `idp-bom` and configure the required plugins yourself.
