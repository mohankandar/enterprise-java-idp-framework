# IDP :: BOM

## Overview

This Bill of Materials (BOM) module defines the dependency versions used across the IDP framework and consumer services.

## What It Provides

- Centralized version management for Spring Boot and third-party libraries
- Consistent dependency alignment across all IDP modules
- Simpler upgrades by changing versions in one place

## Typical Usage

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.mohankandar.idp</groupId>
      <artifactId>idp-bom</artifactId>
      <version>0.1.2-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Notes

- Import this BOM when you use your own corporate parent POM instead of `idp-parent`.
- If your service already inherits from `idp-parent`, you typically do not need to import the BOM directly.
