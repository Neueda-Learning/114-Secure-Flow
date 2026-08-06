# Requirements specification

## Purpose and status

This document gives stable identifiers to behavior verified in the current
repository. It is a **current reconstruction from source, tests, UI, and GitHub
history**, not a claim that these exact requirements existed before
implementation. Product-owner confirmation is required for business intent.

## Functional requirements

| ID | Requirement | Acceptance evidence | Status |
|---|---|---|---|
| `FR-01` | The system shall accept a valid INR transaction and assign server timestamps. | `TransactionController`, `TransactionService`, `createsAndSearchesATransaction` | Verified |
| `FR-02` | The system shall reject invalid identifiers, amounts, currencies, ranges, and malformed values with an HTTP error. | Request DTO annotations, service range checks, `ApiErrorHandler`, `badRequestsHaveClearErrors` | Verified for tested cases |
| `FR-03` | The system shall support transaction text, ID, amount, time, and page filters. | `TransactionRepository.search`, controller parameters, test coverage | Partially verified; not every parameter combination tested |
| `FR-04` | A transaction strictly above INR 10,000 shall generate a HIGH amount alert. | Configuration, `MonitoringService`, `amountAndVelocityRulesCreateAlerts` | Verified at 10,000.01; exact-equality boundary is source-inspected, not directly asserted |
| `FR-05` | More than five transactions for one account within ten minutes shall generate a HIGH velocity alert linked to the window transactions. | `MonitoringService`, repository query, velocity test | Verified for sixth transaction; broader time boundaries partially verified |
| `FR-06` | The first recorded account/payee combination shall generate a MEDIUM new-payee alert. | Pre-save existence check, `MonitoringService`, creation test | Verified for first use; repeat suppression inferred from source and demo behavior |
| `FR-07` | The system shall list and page alerts by status and severity. | `AlertController`, `AlertRepository`, filter test | Partially verified |
| `FR-08` | The system shall expose alert detail with triggering transactions and chronological status history. | `AlertDetailResponse`, entity mappings, lifecycle test | Verified |
| `FR-09` | Alert transitions shall follow `OPEN -> ACKNOWLEDGED -> INVESTIGATING -> CLOSED`, with dismissal allowed from acknowledged/investigating and terminal states immutable. | `AlertService.isAllowed`, lifecycle/dismissal tests | Verified for representative valid/invalid paths |
| `FR-10` | Closing or dismissing shall require trimmed resolution notes of at least three characters. | `AlertService.updateStatus`, dismissal test | Verified |
| `FR-11` | The dashboard shall expose all-time transaction count, total alert count, active-alert count, and transaction volume. | `DashboardService`, dashboard test | Verified for one dataset; aggregation boundaries partially verified |
| `FR-12` | The UI shall display transaction entry/search, current alerts, alert history, rule cards, pagination, details/actions, summary data, and charts. | Static assets, static-page assertion, and Playwright transaction/chart journey | Partially verified; the primary journey is automated while all pages/states are not yet exhaustively covered |
| `FR-13` | A manual demo request shall add a fresh synthetic batch with current timestamps; startup seeding shall run only when enabled and the transaction table is empty. | Demo services/configuration and `eachDemoRequestAddsAFreshBatch` | Manual path verified; startup skip path source-inspected, not directly tested |
| `FR-14` | The system shall expose effective monitoring rules, health, Swagger UI, and OpenAPI description. | Controllers/configuration/dependencies | Rules and static page tested; health/Swagger/OpenAPI are not directly asserted in the current suite |

## Quality and delivery requirements

| ID | Requirement | Evidence | Status |
|---|---|---|---|
| `QR-01` | Production source shall compile on Java 21 and package as an executable JAR. | `pom.xml`, Maven Wrapper, EVD-CI-001 | Verified |
| `QR-02` | The Maven `verify` lifecycle shall run tests and enforce at least 70% measured line coverage with documented exclusions. | `pom.xml`, EVD-CI-001 | Verified for referenced CI run |
| `QR-03` | Flyway shall create/validate the schema and Hibernate shall validate entity mappings. | V1 migration, configuration, H2 tests, MySQL-backed Compose startup | Verified against H2 and locally against MySQL 8.4; CI reproduction awaits publication |
| `QR-04` | Local Compose shall start MySQL before the application and retain data in a named volume. | `compose.yaml`, runtime smoke scripts, local WSL runtime/restart evidence | Verified locally: healthy start, API/database round-trip, and 21 rows retained across stop/start |
| `QR-05` | The application container shall run as a non-root user and expose a health check. | `Dockerfile` | Verified by source inspection; runtime UID/health inspection is recommended |
| `QR-06` | Pull requests and pushes to `main` shall run the Maven gate and Docker build; pushes shall attempt GHCR publication. | Workflow and GitHub runs | Verified; all application stages passed and registry permissions are the isolated follow-up |
| `QR-07` | Significant claims shall be linked to reproducible source, test, configuration, or CI evidence and limitations. | Documentation network | Implemented and locally validated; focused PR publication is next |
| `QR-08` | Sensitive values and real data shall not be committed. | Ignore rules/policy and limited pattern review | Source safeguards and scoped marker check verified; dedicated history scanning is recommended |
| `QR-09` | The default Compose bind shall be local-only. | `127.0.0.1:8080:8080` | Verified by source inspection |
| `QR-10` | Manual load tests shall remain outside Maven, startup, and CI. | `load-tests/`, workflow | Verified by source inspection |

## Business rules and definitions

- **Active alert:** `OPEN`, `ACKNOWLEDGED`, or `INVESTIGATING`.
- **New payee:** no earlier retained transaction has the same normalized
  `accountId` and `payeeId`.
- **Amount match:** amount is strictly greater than the configured threshold.
- **Velocity match:** repository result count is strictly greater than the
  configured maximum in the calculated window.
- **All-time dashboard:** current summary queries do not apply a “today” filter.
- **Server timestamp:** transaction time and creation time come from
  `Instant.now()`; clients do not submit them.

## Assumptions and exclusions

The rules are deliberately demonstrative and explainable. Detection-accuracy,
false-positive, regulatory, customer-suitability, and performance-SLO studies
are clearly reserved for any future operational use. Current k6 thresholds are
transparent test-script criteria.

## Traceability and change control

Full file/test/issue/PR/evidence mappings are in the
[traceability matrix](traceability-matrix.md). A change to behavior must update
this document, the corresponding test/evidence, and any affected risk or
architecture decision.
