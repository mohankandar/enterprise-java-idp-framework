# IDP :: Build Config

## Overview

Shared static-analysis resources used by `idp-parent` and consumer services.

## What It Provides

### Checkstyle
- `src/main/resources/checkstyle/checkstyle.xml` — shared code-style rules

### SpotBugs
- `src/main/resources/spotbugs/base.xml` — baseline SpotBugs rules
- `src/main/resources/spotbugs/exclude.xml` — shared exclusions
- `src/main/resources/spotbugs/framework.xml` — framework-focused SpotBugs profile
- `src/main/resources/spotbugs/jpa-app.xml` — JPA application profile

## Typical Usage

- Reused by `idp-parent` when static-analysis plugins are enabled
- Referenced by framework and consumer builds to keep Checkstyle and SpotBugs rules aligned

## Notes

- Keep static-analysis rules versioned with the framework.
- Use this module as the single source of truth for shared quality gates.

