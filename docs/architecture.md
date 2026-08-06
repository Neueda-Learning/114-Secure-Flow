# Architecture

## Purpose, scope, and status

This document describes the architecture implemented on reviewed `main` commit
`9379af1`. It is intended for developers, maintainers, reviewers, and operators.
Design rationale not present in history is explicitly recorded as a current
reconstruction in [architecture decisions](decisions/README.md).

## System context

```text
Trusted local browser/API client
              |
              | HTTP/JSON (trusted local boundary; TLS/identity next for shared use)
              v
     Spring Boot application
       |                 |
       | JPA/JDBC        +-- static HTML/CSS/JavaScript
       v
      MySQL

Build/delivery boundary:
GitHub -> GitHub Actions -> Maven/Docker -> attempted GHCR publication
```

The application is intentionally self-contained; bank/payment, identity, email,
analytics, and AI integrations are reserved for approved future scope.

## Runtime components

| Component | Responsibility | Implementation |
|---|---|---|
| Browser UI | Entry, filters, pagination, alert review, charts, demo action | [`static/`](../src/main/resources/static/) |
| Transaction component | Validate, normalize, timestamp, save, and search transactions | [`transaction/`](../src/main/java/com/neueda/secureflow/transaction/) |
| Monitoring component | Apply amount, velocity, and new-payee checks | [`monitoring/`](../src/main/java/com/neueda/secureflow/monitoring/) |
| Alert component | Create alerts, link transactions, filter, return detail, enforce transitions | [`alert/`](../src/main/java/com/neueda/secureflow/alert/) |
| Dashboard component | Calculate all-time aggregate counts and volume | [`dashboard/`](../src/main/java/com/neueda/secureflow/dashboard/) |
| Demo component | Generate synthetic batches through the real transaction service | [`demo/`](../src/main/java/com/neueda/secureflow/demo/) |
| Common/config | Page/error models and typed monitoring values | [`common/`](../src/main/java/com/neueda/secureflow/common/), [`config/`](../src/main/java/com/neueda/secureflow/config/) |
| MySQL | Runtime persistence | [`compose.yaml`](../compose.yaml) |
| Flyway | Versioned schema creation/validation | [`db/migration/`](../src/main/resources/db/migration/) |

All components run in one process and one deployable JAR. Package boundaries
are organizational conventions, not independently deployed services.

## Transaction creation and monitoring data flow

```text
POST /api/transactions
        |
        v
TransactionController: Bean Validation
        |
        v
TransactionService: trim identifiers, uppercase/validate INR, validate range
        |
        +--> repository checks whether account/payee existed before save
        |
        v
save transaction with Instant.now()
        |
        v
MonitoringService
   | amount > limit? ---------> create HIGH alert
   | count in window > max? --> create HIGH alert linked to window rows
   | first account/payee? ----> create MEDIUM alert
        |
        v
return transaction plus alerts
```

[`TransactionService.create`](../src/main/java/com/neueda/secureflow/transaction/TransactionService.java)
has a Spring transaction boundary. The saved transaction and generated alerts
participate in that database transaction; an unhandled persistence failure
should roll it back. This does not provide distributed transaction behavior.

## Monitoring semantics

### High amount

The check is strictly greater than `monitoring.amount-limit`; equality does not
match. Defaults are INR 10,000 and severity HIGH.

### Velocity

The service calculates:

```text
window start = transaction time - configured minutes
window end   = transaction time + 1 second
```

It queries the same account between those values and creates a HIGH alert when
the returned count is greater than five. The one-second extension includes the
just-saved transaction despite timestamp/database precision differences. That
implementation choice is source-inspected but not documented by a dedicated
boundary test.

### New payee

Before the save, the repository checks whether any retained transaction already
uses the normalized account/payee pair. On first use, a MEDIUM alert is created.
The current baseline uses a simple first-ever definition; a time-based cooldown
is a documented extension, and deletion/reset naturally starts a fresh dataset.

## Alert lifecycle

```text
OPEN -> ACKNOWLEDGED -> INVESTIGATING -> CLOSED
            |                |
            +-> DISMISSED <---+
```

- `CLOSED` and `DISMISSED` are terminal.
- Dismissal and closure require trimmed notes of at least three characters.
- Every alert begins with an initial history row.
- Each accepted transition adds a history row and applicable timestamp.
- Status timestamps/history are already recorded; authenticated actor identity
  is the planned shared-environment audit enhancement.

## Database design

| Table | Purpose | Important relationships/indexes |
|---|---|---|
| `transactions` | Normalized payment details and server timestamps | Account/time and account/payee indexes |
| `alerts` | Rule result and current lifecycle fields | Status index |
| `alert_transactions` | Many-to-many alert/transaction links | Composite primary key and foreign keys |
| `alert_status_history` | Append-on-transition history | Foreign key to alert; actor column is a future identity extension |
| `flyway_schema_history` | Flyway migration ledger | Managed internally by Flyway |

The schema source is
[`V1__create_tables.sql`](../src/main/resources/db/migration/V1__create_tables.sql).
Runtime uses MySQL 8.4. Fast Maven tests use H2 in MySQL mode and apply the same
migration. The separate Compose system job starts real MySQL 8.4, verifies a
live API/database round-trip, and confirms named-volume continuity. Hibernate
uses `ddl-auto: validate`, so entity mappings are checked rather than used to
mutate the schema.

### Data classification

Demo identifiers are synthetic, establishing a clear learning-data boundary.
If real-data scope is ever approved, account/payee identifiers, descriptions,
amounts, timestamps, notes, and behavioral links can be supported by formal
classification, retention, deletion, masking, encryption, and rights workflows.

## Time handling

- Backend timestamps use `Instant`/UTC.
- Hibernate JDBC and Jackson are configured for UTC.
- The browser formats displayed times for `Asia/Kolkata`.
- Dashboard summaries are all-time; they do not filter to a local “today.”
- Demo data uses the current server time when each transaction is created.

## Browser architecture

The browser loads one static page and calls REST endpoints using `fetch`.
Charts are intentionally constructed from loaded transaction/alert pages in
JavaScript, keeping the implementation approachable. If owners require
full-dataset analytics, a server aggregate endpoint is the natural extension.

The UI uses semantic labels, a skip link, ARIA attributes, focusable table
regions, and keyboard handling for chart tabs. Playwright automates the primary
transaction/chart journey, and axe checks automatically detectable WCAG A/AA
issues in Chromium. Manual assistive-technology and specialist review remain a
separate assurance layer.

## Container and startup flow

```text
docker compose up --build --wait
  -> create network and named mysql-data volume
  -> start MySQL and wait for mysqladmin health check
  -> build application in Maven image (tests skipped inside Docker build)
  -> copy JAR into Java 21 Alpine runtime image
  -> run as non-root secureflow user
  -> Flyway migrates, Hibernate validates, optional startup seed runs
  -> application health check queries /actuator/health
```

The Docker build stays efficient by relying on CI/local `mvnw clean verify` as
the preceding quality gate. Together, the Maven result and Docker build provide
distinct, traceable evidence.

## Trust boundaries

1. Browser/client to unauthenticated HTTP application.
2. Application to database using environment-provided credentials.
3. Host/container boundary and named-volume persistence.
4. Developer/GitHub to hosted runner, Maven Central, base-image registries, and
   GHCR.
5. Future Linux deployment boundary in draft PR #46 (not current).

Security implications are analyzed in
[security and threat model](security-and-threat-model.md).

## Deliberate simplifications and consequences

- One deployable improves learnability but couples scaling/release.
- Direct rule conditionals are readable but can become hard to extend.
- One integration-test class is easy to follow but provides limited test
  categorization/isolation.
- H2 remains intentionally fast; the Compose job provides broader MySQL parity,
  while targeted database edge cases can be added as SQL complexity grows.
- The static frontend stays dependency-free at runtime; browser-only dev
  dependencies are locked and used solely for Playwright/axe verification.
- Startup migrations simplify local setup but couple database privileges to
  application startup.

## Related evidence and decisions

- [Requirements](requirements.md)
- [API reference](api.md)
- [Testing](testing.md)
- [ADR-001](decisions/ADR-001-modular-monolith-and-static-ui.md)
- [ADR-002](decisions/ADR-002-flyway-and-hibernate-validation.md)
- [ADR-003](decisions/ADR-003-integration-tests-with-h2.md)
- [Evidence index](evidence-index.md)

## Maintenance

Update this document with any component, schema, trust-boundary, data-flow,
time, or deployment change. Add/supersede an ADR for material design changes.
