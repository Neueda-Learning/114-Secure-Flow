# Testing strategy and verified evidence

## Purpose, scope, and audience

This document explains when tests run, how success is decided, what the current
suite covers, and which complementary verification layers are planned. It is
for developers, reviewers, mentors, and operators, and keeps each successful
result connected to the exact checks that produced it.

## When automated tests run

- Locally only when a developer executes Maven test/verify goals.
- In GitHub Actions for every pull request.
- In GitHub Actions for every push to `main`.
- Application startup stays fast because tests run during the Maven quality
  phase rather than when the normal JAR starts.
- The Dockerfile packages with `-DskipTests`; CI first runs `clean verify`, then
  independently exercises the built image against MySQL through Compose.
- Playwright/axe checks run in the separate container-system job after the live
  MySQL-backed application passes its health and API smoke checks.
- Manual k6 scenarios remain intentionally separate so load is generated only
  in an approved test environment.

Flyway migration is separate from test scheduling: it runs whenever the Spring
application/test context starts with Flyway enabled.

## Quality-gate command and pass/fail logic

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS/CI:

```bash
./mvnw clean verify
```

```text
clean -> compile -> test -> package -> JaCoCo report -> JaCoCo check -> verify
```

Maven succeeds only when every required phase exits successfully. Examples of
failure causes are compilation error, context/migration startup error, failed
assertion, unexpected exception, test timeout, packaging error, or JaCoCo line
coverage below 70%. A successful Maven command returns exit code 0.

The GitHub job then builds the Dockerfile. For pushes to `main`, it also tries
to publish the image. Therefore an overall workflow can fail after all tests
pass, as happened on the reviewed latest `main` run.

## Automated suite design

Source:
[`SecureFlowTest.java`](../src/test/java/com/neueda/secureflow/SecureFlowTest.java)

- `@SpringBootTest` loads real Spring application components.
- `@AutoConfigureMockMvc` provides in-process HTTP calls without a listening
  network port.
- H2 is an in-memory database in MySQL compatibility mode.
- Flyway applies the production V1 SQL migration.
- Hibernate validates the entity/schema mapping.
- `@BeforeEach` deletes alerts and transactions for test isolation.

These are integration/end-to-end-at-the-HTTP-layer tests. The separate
Playwright suite supplies the external browser/network/system layer.

## Seven Maven integration scenarios

| Test method | Purpose | Main assertions | Next coverage layer |
|---|---|---|---|
| `createsAndSearchesATransaction` | Creation, trimming, currency, new-payee alert, basic search | `201`, normalized values, one search result | Add remaining validation/filter combinations |
| `amountAndVelocityRulesCreateAlerts` | Amount and sixth-in-window velocity alerts | Rule types and successful creations | Add exact-threshold and time-window edge assertions |
| `alertMovesThroughTheCompleteStatusFlow` | Valid lifecycle and invalid early/terminal moves | `409` invalid moves, final status, four history rows, linked transaction | Add concurrency and future authorization coverage |
| `alertCanBeDismissedWithAReason` | Dismissal and minimum note length | Short note fails; valid note succeeds/filter finds it | Add maximum-length boundary assertion |
| `badRequestsHaveClearErrors` | Invalid payload/currency/ranges/time/status/missing ID | Representative `400`/`404` and titles | Expand malformed JSON/type/paging boundaries |
| `dashboardRulesFiltersAndWebPageWork` | Summary/rules/severity filter/static HTML markers | Counts, three rules, static content | Browser behavior is complemented by Playwright; retain targeted static assertions |
| `eachDemoRequestAddsAFreshBatch` | Repeated manual demo batches | 20 transactions/12 alerts each; totals after two calls | Add a direct startup `seedIfEmpty` path assertion |

## Test categories

| Category | Purpose/tools | Current verified result | Evidence | Next coverage layer |
|---|---|---|---|---|
| Unit | Isolate one class/rule | Integration suite already exercises the rules through HTTP | Current integration source | Add focused unit boundaries as rule complexity grows |
| Integration | HTTP -> service -> repository -> migration using Spring/MockMvc/H2 | 7 passed in CI and locally | [EVD-CI-001](evidence-index.md#evd-ci-001-main-quality-gate-and-registry-follow-up), [EVD-SYSTEM-001](evidence-index.md#evd-system-001-mysql-compose-and-runtime-verification) | Add focused real-MySQL repository edge cases if SQL complexity grows |
| End-to-end | External browser/network/container flow | Two Playwright checks passed against the MySQL-backed Compose stack | [EVD-BROWSER-001](evidence-index.md#evd-browser-001-browser-and-automated-accessibility-verification) | Add alert lifecycle and paging journeys |
| Manual UI | Visual/interaction/viewport review | Historical PR descriptions provide a starting record | PR bodies where linked | Use a dated checklist with browser/viewport/result |
| Regression | Re-run current suite for changes | CI runs suite on PR/main | Workflow and run history | Require green checks through delivery-relevant steps |
| Failure path | Invalid payload/ranges/transitions/missing data | Representative cases pass | `badRequests...`, lifecycle tests | Add DB outage, migration failure, concurrency, malformed/body-size cases |
| Input validation | Browser constraints + Bean Validation + service rules | Representative backend cases pass | DTO/service/test source | Add parameterized min/max/boundary tests; browser constraints are bypassable |
| Security | Vulnerability, authorization, secret, dependency tests | Source review and security guidance completed | Security documentation | Add SAST, dependency/container/secret scanning and authorization tests after identity exists |
| Performance | Manual k6 gradual/spike | Two scenarios exist and PR #44 records reported results | [EVD-PR-044](evidence-index.md#evd-pr-044-manual-load-test-contribution) | Re-run in a controlled environment and retain immutable output/methodology |
| Accessibility | Keyboard, semantics, contrast, screen reader | axe found no automatically detectable WCAG A/AA violations in the tested Chromium page state | EVD-BROWSER-001 | Add manual keyboard, screen-reader, zoom/reflow, and specialist review; automation cannot prove full conformance |
| Compatibility | Browser/OS/database combinations | Chromium on Linux container plus MySQL 8.4 Compose passed locally | EVD-SYSTEM-001, EVD-BROWSER-001 | Add Firefox/WebKit and an agreed OS/browser support matrix |
| Deployment | Compose/health/restart/persistence | Disposable Compose start, health, API round-trip, non-root UID, and row-count persistence across stop/start passed | EVD-SYSTEM-001 | Add backup/restore, migration-upgrade, rollback, and supervised target-host evidence |

## Fast test database and MySQL parity layer

[`src/test/resources/application.yml`](../src/test/resources/application.yml)
uses H2 2.4.240 in MySQL mode for fast, repeatable feedback. All tests passed.
The Flyway version signal and known H2/MySQL differences make the real-MySQL
Compose job an important complementary check. It starts MySQL 8.4, lets Flyway
apply the production migration, creates a transaction through the live API,
searches it back, confirms a database row, and proves the named volume retains
the row count across a Compose stop/start.

Current layered approach:

1. Retain H2 integration tests for fast feedback.
2. Run a disposable MySQL/Compose system test in CI.
3. Run external Chromium and axe checks against that live stack.
4. Add narrower Testcontainers repository cases later only where direct SQL or
   migration complexity justifies the extra test layer.

## JaCoCo evidence and interpretation

`pom.xml` configures JaCoCo 0.8.14 to instrument tests, generate HTML, and fail
`verify` when measured bundle line coverage is below 70%.

Excluded from the minimum gate:

- `*Application.class`
- entity classes
- DTO packages
- config packages

The reviewed CI logs show the configured threshold passed. The local
2026-08-06 report covered 334 of 344 reported lines (97.09%). This percentage
is tied to that worktree/run and will change with code or tests. JaCoCo indicates
executed bytecode; JUnit assertions decide expected behavior.

## CI evidence snapshot

Latest reviewed `main` workflow:

- Run: [31084398909](https://github.com/Neueda-Learning/114-Secure-Flow/actions/runs/31084398909)
- Commit: `9379af1`
- Maven: 7 tests, 0 failures, 0 errors, 0 skipped
- Flyway: 1 migration validated/applied in H2 test context
- JaCoCo/Maven: passed
- JAR artifact upload: passed
- Docker image construction: passed
- GHCR image publication: permission follow-up required (step status `failed`,
  response `denied`)
- Overall workflow: failed at final registry delivery after every application
  quality and construction stage passed

This quality and delivery-step evidence remains valid for that commit and
environment.

## Final branch verification snapshot

The review branch was also checked locally with:

```powershell
.\mvnw.cmd --batch-mode clean verify
```

On 2026-08-06, using Windows 11 and Java 21.0.12, the command exited 0. It
reported 7 tests, 0 failures, 0 errors, 0 skipped; applied Flyway migration V1
to H2; built the executable JAR; met the JaCoCo check; and produced 97.09%
reported line coverage (334 covered, 10 missed).

The same branch then passed these WSL/Docker checks:

```bash
COMPOSE_PROJECT_NAME=secureflow-audit APP_PORT=18080 \
  docker compose up --build --detach --wait
APP_BASE_URL=http://127.0.0.1:18080 \
  COMPOSE_PROJECT_NAME=secureflow-audit APP_PORT=18080 \
  bash scripts/verify-running-app.sh
```

Verified results were health `UP`, an API create/search round-trip, 21 MySQL
rows after the smoke transaction, and application UID 100. Browser automation
then passed two Chromium tests: the submit/chart journey and an axe WCAG A/AA
scan with no automatically detectable violations. The scripted stop/start check
retained the same 21 rows before and after recreation. The disposable
containers, network, and `secureflow-final-check_mysql-data` volume were removed
afterward.

`npm audit` reported zero vulnerabilities for the locked browser-test dependency
set. These results are indexed as EVD-DOC-001, EVD-SYSTEM-001, and
EVD-BROWSER-001. Maven also reported maintenance signals for Flyway/H2 version
alignment and future Mockito agent configuration; neither affected success.

## Manual load-test pass/fail nuance

k6 `check(response, ...)` records a check metric. The scripts define thresholds
for `http_req_failed` and `http_req_duration`, but not for the `checks` metric.
Consequently, a failed `201` check is visible in results but is not itself a
declared threshold failure unless it also affects a configured HTTP metric. See
[manual load-test guide](../load-tests/README.md) before interpreting results.

## Evidence retention

For material releases, confirm GitHub artifact-retention settings and retain
test reports, coverage, image digest/SBOM, migration result, and smoke-test
evidence with a version/commit and environment. Remove sensitive data from logs.

## Maintenance

Update the scenario table, category status, CI snapshot, evidence index, and
traceability whenever tests, thresholds, environments, or workflow steps change.
