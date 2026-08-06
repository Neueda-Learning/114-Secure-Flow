# Current boundaries and improvement roadmap

## Purpose and status

This document helps reviewers see both the verified foundation and its practical
growth path. Each boundary is paired with a clear enhancement opportunity; the
descriptions apply to the reviewed `main` baseline.

## Product and data

- The product has a deliberately focused INR scope, keeping validation and
  demonstrations consistent.
- Three deterministic monitoring rules are easy to explain and reproduce;
  accuracy and regulatory validation can be added if the use case expands.
- “New payee” has a precise first-ever retained-pair definition; a configurable
  cooldown is a documented future extension.
- All-time dashboard totals provide a stable aggregate view. A date-range view
  can be added when reporting requirements are confirmed.
- Browser-rendered charts keep the frontend simple. A server aggregate endpoint
  is the natural next step if complete-dataset charting is required.
- Payment integration, customer profiles, case assignment, approval, escalation,
  and regulatory reporting are intentionally reserved for domain-approved scope.
- Demo seeding quickly creates realistic synthetic presentation data; access
  control is the planned safeguard before any shared-environment use.

## Security, privacy, and compliance

- Current controls include loopback-only binding, a non-root application
  container, server-side validation, constrained alert transitions, and a
  versioned history model.
- Example credentials support quick local setup and are clearly labelled for
  replacement before shared use.
- Identity, authorization, actor attribution, TLS, and rate controls form the
  next shared-environment security layer.
- Managed secrets, scanning, SBOM, signing, and patch ownership provide a clear
  supply-chain maturity path.
- Retention, deletion/export, legal hold, backup, restore, incident response,
  and operational monitoring are prioritized before real-data use.
- Legal, privacy, accessibility, security, and sector conclusions are correctly
  reserved for qualified review, preventing unsupported compliance claims.

## Testing and quality

- Seven readable HTTP integration tests verify the main transaction, alert,
  validation, dashboard, and demo flows; focused unit tests can complement them
  as rule complexity grows.
- H2 in MySQL mode gives fast one-command feedback, while the disposable
  Compose job now supplies a real MySQL 8.4 migration/API/persistence layer.
- Two Playwright checks exercise a live transaction/chart journey and automated
  WCAG A/AA rules in Chromium; broader states, browsers, keyboard, screen-reader,
  zoom/reflow, and specialist review remain the natural accessibility path.
- The 70% JaCoCo gate is automated and its exclusions are transparent; future
  iterations can expand measured classes and branch/boundary assertions.
- Browser, automated accessibility, MySQL, and Compose checks are now integrated
  into the pipeline; security scanning and wider compatibility remain the next
  complementary automation layers.
- Two k6 scenarios already provide gradual and 1,000-user load generation. The
  next evidence run will retain raw output, environment details, and an explicit
  current-port setting.

## Delivery and operations

- The latest reviewed pipeline passed tests, Flyway, JaCoCo, JAR upload, and
  Docker construction. GHCR permission alignment is the single remaining
  registry-delivery action for that run.
- The review branch adds reproducible health/API/MySQL/non-root/restart checks
  to the existing local Compose deployment; draft PR #46
  separately explores Linux server automation for supervised verification.
- The local single-instance design is easy to operate and explain. Resource
  policies, centralized logs, backup, rollback, and resilience are the planned
  steps for a shared environment.
- The simple `:latest` convention suits the learning workflow; immutable SHA
  tags and digests are recommended for release traceability.

## Governance

- The repository shows multi-contributor issues, branches, PRs, and CI history,
  and `main` already enforces pull requests, one approval, and
  `test-and-package`. After the first revised workflow run, requiring the new
  MySQL/browser check, stale-approval dismissal, and conversation resolution
  would extend this verified control.
- The documented branch/PR workflow now provides consistent expectations. A
  release-tag/versioning policy and review of long-lived branches are logical
  maintenance steps.
- Selecting a repository license will make redistribution terms explicit.
- Historical issues/PRs are preserved authentically, while the repository
  history document supplies current navigation and context.
- AI assistance is transparently disclosed; recorded independent human review
  can provide the final verification layer before merge.

## Related work

- [Risk register](risk-register.md)
- [Future scope](future-scope.md)
- [Security and threat model](security-and-threat-model.md)
- [Testing](testing.md)

## Maintenance

Promote an item from roadmap to implemented status after merged implementation
and verification evidence are linked. Track remaining work through requirements
or issues with an owner and priority so progress stays visible.
