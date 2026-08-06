# OWASP Top 10 (2021) Security Assessment — SecureFlow

**Status:** Local working document, not yet committed/pushed.
**Scope:** Reviewed against the actual codebase and the project's stated
requirements (single operator, no authentication required — see
"Project Requirements Alignment" section below).
**Date of assessment:** 2026-08-06

---

## 1. Project Requirements Alignment (read this first)

The project brief explicitly states:

> "There will be no authentication and a single operator is assumed, i.e.
> there is no requirement to manage multiple users or operators."

This directly changes how two OWASP categories (A01, A07) should be judged.
They are **not defects** for this exercise — they are intentional, spec-aligned
scope decisions. They are still documented below because:
- a real OWASP review would flag them regardless of project scope, and
- the presentation guidance explicitly asks what you'd do differently /
  next with more time — this is good material for that section.

---

## 2. Findings by Category

### A01: Broken Access Control
**Status: Covered by design (in-scope waiver from requirements)**

- No authentication/authorization exists anywhere in the codebase. Every
  endpoint in `TransactionController`, `AlertController`, `RuleController`,
  `DashboardController`, and `DemoDataController` is fully public.
- Per the project brief's single-operator assumption, there are no user
  boundaries to violate, so IDOR-style access control issues do not apply.
- **Verdict:** Compliant with stated requirements. No action needed for
  this exercise.
- **Future scope (later sprint):** If this ever supports multiple
  operators, add Spring Security with role-based access (e.g., Analyst can
  view/acknowledge, Supervisor can close/dismiss).

### A02: Cryptographic Failures
**Status: Partially covered / gap identified**

- No TLS termination inside the app itself; relies entirely on the
  deployment environment (reverse proxy / infra).
- Weak fallback DB password baked into config as a default:
  `src/main/resources/application.yml` — `password: ${DB_PASSWORD:secureflow123}`.
  If `DB_PASSWORD` is ever unset in a real deployment, the app silently
  falls back to a well-known weak password instead of failing to start.
- **Verdict:** Acceptable for local/classroom use (matches `.env.example`
  intent), but the silent-fallback pattern is a real risk if reused
  elsewhere.
- **Future scope (later sprint):**
  - Remove the hardcoded fallback value so misconfiguration fails loudly.
  - Terminate TLS via a reverse proxy (nginx/Caddy) for any non-classroom
    deployment.
  - Move credentials to a secrets manager (AWS Secrets Manager/SSM) if
    ever used beyond training.

### A03: Injection
**Status: Covered — no action needed**

- All persistence goes through Spring Data JPA/Hibernate with parameterized
  queries; no raw/native SQL string concatenation found anywhere.
- Input validation already enforced with allow-list regex patterns on
  free-text identifiers: `CreateTransactionRequest.accountId` /
  `.payeeId` use `@Pattern(regexp = "\\s*[A-Za-z0-9-]+\\s*")`.
- Evidence: `src/main/java/com/neueda/secureflow/transaction/dto/CreateTransactionRequest.java`
- **Verdict:** Well mitigated already. Keep using Specification/Criteria
  API rather than string-built queries as the project grows.

### A04: Insecure Design
**Status: Mostly covered, minor gap noted for future scope**

- `/api/demo/seed` (`DemoDataController`) has no rate limiting and can be
  called repeatedly to keep inserting synthetic data. This directly maps
  to the brief's Appendix H "test data generator" concept, so it is
  expected/desired functionality for this exercise, not a design flaw in
  context.
- **Verdict:** Acceptable as-is for the training exercise's stated use
  case (generating demo/test transactions on demand).
- **Future scope (later sprint):** Add basic rate limiting (e.g., Bucket4j)
  on write endpoints if this is ever exposed beyond the team/instructor,
  and restrict demo-seeding to a non-production profile only.

### A05: Security Misconfiguration
**Status: Covered — Swagger exposure is requirement-aligned, not a gap**

- Actuator exposure is minimal and intentional: `management.endpoints.web.exposure.include: health,info`.
- Swagger UI is public and unauthenticated at `/swagger-ui.html` — the
  project brief's Notes item 4 explicitly recommends Swagger/OpenAPI
  documentation, so this is a **requirement-aligned** decision, not a
  misconfiguration, given the no-auth/single-operator scope.
- Docker container already runs as a non-root user (`secureflow`):
  `Dockerfile` — `RUN addgroup -S secureflow && adduser -S secureflow -G secureflow` /
  `USER secureflow`.
- `DEMO_SEED_ON_STARTUP=true` was observed set in the EC2 test deployment
  environment — appropriate for demos, would need to default to `false`
  if this were ever reused for a real environment.
- **Verdict:** Covered / acceptable for this exercise.
- **Future scope (later sprint):** Disable or protect Swagger UI behind
  auth only if this project is ever repurposed beyond training/demo use.

### A06: Vulnerable and Outdated Components
**Status: Gap identified**

- Spring Boot 4.0.7 is current at time of review — good baseline.
- No automated dependency vulnerability scanning exists in
  `.github/workflows/pipeline.yml` (no Dependabot, OWASP Dependency-Check,
  or Snyk step).
- **Future scope (later sprint):** Add GitHub Dependabot or a
  Dependency-Check CI step for automated CVE alerts on Maven dependencies
  and base Docker images (`maven:3.9.11-eclipse-temurin-21`,
  `eclipse-temurin:21-jre-alpine`).

### A07: Identification and Authentication Failures
**Status: Covered by design (in-scope waiver from requirements)**

- Same justification as A01 — the project brief explicitly waives this
  requirement for this exercise (single operator, no auth).
- **Verdict:** Compliant with stated requirements.
- **Future scope (later sprint):** Add real authentication (OAuth2/OIDC,
  or at minimum hashed-credential login) only if this project moves to
  supporting multiple operators or handling real financial data.

### A08: Software and Data Integrity Failures
**Status: Gap identified, low priority for this exercise**

- CI builds and publishes a Docker image to GHCR
  (`.github/workflows/pipeline.yml`), but there is no image signing, SBOM
  generation, or dependency integrity verification step.
- **Future scope (later sprint):** Add SBOM generation (e.g., CycloneDX
  Maven plugin) and consider image signing (cosign) if this pipeline is
  ever trusted for real deployment decisions.

### A09: Security Logging and Monitoring Failures
**Status: Core requirement covered; advanced enhancement out of scope**

- Since there is only one operator, "who made this change" is less
  meaningful for this exercise. The project's Appendix D lists "track who
  acknowledged/closed each alert" as an **advanced/optional enhancement**,
  not a core requirement.
- The core requirement — a full alert lifecycle audit trail (status +
  timestamp + resolution notes) — is already implemented via
  `AlertHistoryEntity` and surfaced through `AlertDetailResponse.history`.
- **Verdict:** Core requirement covered. Strength to highlight in the
  presentation.
- **Future scope (later sprint):** Add per-operator identity to the audit
  trail (Appendix D item 8) if/when multi-operator support is added.

### A10: Server-Side Request Forgery (SSRF)
**Status: Not applicable**

- No outbound HTTP requests are made based on user-supplied input anywhere
  in the current codebase.
- **Future scope (later sprint):** Only relevant if a future feature adds
  webhook URLs or external API calls driven by user input (e.g.,
  Appendix D item 7, "Webhook integration for external systems"). Add an
  allow-list for outbound destinations at that time.

---

## 3. Summary Table

| # | Category | Status | Action needed now? |
|---|---|---|---|
| A01 | Broken Access Control | Covered by design (requirement waiver) | No |
| A02 | Cryptographic Failures | Gap (weak default password fallback) | Optional cleanup |
| A03 | Injection | Covered | No |
| A04 | Insecure Design | Covered (demo-seed matches spec) | Optional (rate limiting) |
| A05 | Security Misconfiguration | Covered (Swagger is requirement-aligned) | No |
| A06 | Vulnerable/Outdated Components | Gap (no scanning in CI) | Recommended (low effort) |
| A07 | Auth Failures | Covered by design (requirement waiver) | No |
| A08 | Software/Data Integrity Failures | Gap (no SBOM/signing) | Low priority |
| A09 | Logging/Monitoring Failures | Core requirement covered | No |
| A10 | SSRF | Not applicable | No |

---

## 4. Presentation Talking Points

1. Two commonly-flagged OWASP items (A01/A07) are explicitly waived by the
   project's own stated requirements — present this confidently as a scope
   decision, not an oversight.
2. Swagger UI being public is intentional and requirement-aligned
   (Notes item 4), not a misconfiguration.
3. The rule engine already follows the Strategy-pattern design the brief
   recommends in Appendix G (`MonitoringRule` interface with pluggable
   `AmountThresholdRule`/`VelocityRule`/`NewPayeeRule` implementations).
4. The alert audit trail (`AlertHistoryEntity`) already satisfies the core
   lifecycle requirement from Appendix E's testing considerations.
5. Good "what we'd do next" answers: remove the weak default DB password
   fallback, add dependency scanning, add rate limiting on write
   endpoints, and add real authentication if this becomes multi-operator.
