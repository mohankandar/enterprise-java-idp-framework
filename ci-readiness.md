# CI Readiness

## Current Phase
- **Phase 1 (active)**

## Enabled CI Stages
- **Stage A (PR-blocking):** Framework fast enforcement
- **Stage B (PR-blocking):** Consumer/runtime contract
- **Stage D (Nightly):** Full verify + static analysis + IT gate

## Stage Definitions
| Stage | Modules | Command | Schedule | Owner | Purpose |
|---|---|---|---|---|---|
| Stage A | `idp-core`, `idp-autoconfigure`, `idp-starter-data`, `idp-starter-security`, `idp-starter-platform` | `mvn -pl idp-core,idp-autoconfigure,idp-starter-data,idp-starter-security,idp-starter-platform -am -Dcoverage.guard.skip=false test` | PR | Framework Maintainer | Fast framework enforcement |
| Stage B | `idp-demo-service` | `mvn -pl idp-demo-service -am -Dcoverage.guard.skip=false test` | PR | Demo Service Owner | Consumer/runtime contract enforcement |
| Stage D | full reactor | `mvn -Dtest.it.enabled=true -Dit.guard.skip=false -Dcoverage.guard.skip=false verify` | Nightly | DevEx / Build Owner | Full verify (tests + IT + static analysis) |
| Stage E | `idp-starter-security`, `idp-starter-platform`, `idp-demo-service` | `mvn -pl idp-starter-security,idp-starter-platform,idp-demo-service -am -Dcoverage.guard.skip=false -Dspring-boot-dependencies.version=<BOOT_VERSION> -Dplugin.version.spring-boot=<BOOT_VERSION> test` and `mvn -pl idp-demo-service -am -Dtest.it.enabled=true -Dit.guard.skip=false -Dspring-boot-dependencies.version=<BOOT_VERSION> -Dplugin.version.spring-boot=<BOOT_VERSION> verify` | Nightly (Phase 2+) | Framework Maintainer + DevEx / Build Owner | Spring Boot upgrade smoke and drift detection |

## What Each Stage Enforces
- **Stage A**
  - Core contract behavior (`ApiResponse`, error model, correlation utilities)
  - Starter auto-configuration conditions and back-off behavior
  - Security/filter wiring invariants in framework starters
- **Stage B**
  - Consumer-facing API envelope/error behavior in demo service
  - Runtime request behavior through MVC layer
  - Contract drift detection from framework changes

## Blocking vs Nightly
- **PR-blocking:** Stage A, Stage B
- **Nightly-only:** Stage D
- **Nightly-only (Phase 2+):** Stage E

## Metrics
- **Failure rate metric (A+B):** `Stage A + B failure rate < 2% excluding code failures`
- **Code failures excluded from failure-rate metric:** compilation errors, checkstyle violations, spotbugs violations, merge conflicts, unresolved dependencies.

## Runtime Baseline
- **Baseline definition:** last 7 days median runtime per stage.
- **Runtime spike trigger:** stage runtime > 130% of that baseline median.

## Promotion Criteria to Next Phase (Phase 2)
- 7 consecutive nightly Stage D green runs
- Stage A + B failure rate < 2% excluding code failures over 5 business days
- No manual rerun needed for flaky tests for 5 business days

## Stage Fail Conditions
- **Stage D fails if any of the following occurs:**
  - unit/slice tests fail in any module
  - integration tests fail or are missing when IT gate is enabled (`test.it.enabled=true`, `it.guard.skip=false`)
  - static analysis fails (`checkstyle`, `spotbugs`)
  - Maven `verify` exits non-zero
- **Stage E fails if any of the following occurs:**
  - module tests fail under candidate Spring Boot version
  - demo smoke verify fails under candidate Spring Boot version
  - startup/context wiring regresses in `idp-starter-security`, `idp-starter-platform`, or `idp-demo-service`
  - Maven command exits non-zero in either Stage E command

## Rollback Rule (Flakiness / Runtime Spike)
- Trigger rollback if either condition holds for 2 consecutive days:
  - Stage A or Stage B flakiness > 5% (rerun-required failures / total runs)
  - Stage A or Stage B runtime > 130% of last 7 days median baseline
- Rollback action:
  - Keep Stage A + Stage B PR-blocking
  - Move newly introduced stage(s) back to nightly-only
  - Open incident and assign to owning stage owner

## Mandatory Update Rule
- If CI passes but framework behavior is broken, tests must be updated immediately.

## Optimization Target
- We optimize for **failure visibility, framework enforcement, and upgrade safety**.
- We do **not** optimize for code coverage percentage.
