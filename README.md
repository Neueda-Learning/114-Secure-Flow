# SecureFlow

[![CI and CD](https://github.com/Neueda-Learning/114-Secure-Flow/actions/workflows/pipeline.yml/badge.svg)](https://github.com/Neueda-Learning/114-Secure-Flow/actions/workflows/pipeline.yml)

SecureFlow is a learning and demonstration application for recording INR
transactions, applying three configurable monitoring rules, and managing the
resulting alert lifecycle. It is a Java 21 and Spring Boot modular monolith with
a plain HTML/CSS/JavaScript interface and MySQL persistence.

## Current status

The documented baseline is `main` at commit
[`9379af1`](https://github.com/Neueda-Learning/114-Secure-Flow/commit/9379af19e7194a4a5b8e0a22eb4f34e141b1a503),
reviewed on 2026-08-06.

- Seven Spring Boot HTTP integration tests are present and passed in the
  referenced `main` CI run.
- The 70% JaCoCo line-coverage gate passed in that run.
- The runnable JAR and Docker image build completed.
- The review branch also passed a disposable MySQL 8.4 Compose smoke test,
  named-volume restart check, non-root runtime check, and two Chromium tests.
- The automated axe scan found no automatically detectable WCAG A/AA
  violations in the verified page state. This is useful evidence, not a claim
  of complete accessibility conformance.
- Every application quality and packaging stage completed successfully. The
  remaining registry-delivery step returned `denied`. Package/repository access
  is configured; an organization Actions administrator must now allow package
  write for workflow tokens before publication can be verified.
- GitHub settings verify that `main` requires pull requests, one approval, and
  the existing `test-and-package` check.
- The current branch is intentionally optimized for local learning. Identity,
  TLS, managed secrets, backup, security-scanning, and shared-deployment
  controls are documented as the next maturity stage.

See the [evidence index](docs/evidence-index.md) and
[improvement roadmap](docs/known-limitations.md) for the verified boundary and
next evidence steps.

## Implemented capabilities

- Create and search INR transactions with server-side and browser validation.
- Search by text, transaction ID, amount range, and time range.
- Page transaction, current-alert, and alert-history results.
- Create alerts for amounts above INR 10,000, more than five transactions in
  ten minutes, and first use of a payee by an account.
- Move alerts through `OPEN`, `ACKNOWLEDGED`, `INVESTIGATING`, and `CLOSED`, or
  dismiss an acknowledged/investigating alert with resolution notes.
- Retain linked triggering transactions and alert status history.
- Display aggregate dashboard data and browser-rendered charts.
- Add synthetic presentation data with current timestamps.
- Expose REST endpoints, generated OpenAPI/Swagger UI, and a health endpoint.
- Run Maven/JUnit/Flyway/JaCoCo, MySQL/Compose, browser, accessibility, and
  container-delivery checks in GitHub Actions.

The authoritative requirement-to-code-and-test mapping is in
[requirements](docs/requirements.md) and the
[traceability matrix](docs/traceability-matrix.md).

## Quick start with Docker

Requirements: Docker Engine or Docker Desktop with Docker Compose v2.

```bash
docker compose config
docker compose up --build --wait
```

Open:

- Dashboard: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Health: <http://localhost:8080/actuator/health>

Stop while keeping database data:

```bash
docker compose down
```

Delete the containers **and the named MySQL data volume**:

```bash
docker compose down --volumes
```

The default credentials in `compose.yaml` and `.env.example` are for local
learning only. Do not use them for a shared or public environment.

## Demo data

Docker enables startup seeding. Startup seeding adds one synthetic batch only
when the transaction table is empty. A manual request always adds another
batch of 20 transactions using the real application service and current server
timestamps:

```bash
curl -X POST http://localhost:8080/api/demo/seed
```

The current automated test expects each manual batch to produce 12 alerts.
That number is implementation-specific and should be updated if monitoring
rules change.

Set `DEMO_SEED_ON_STARTUP=false` to disable startup seeding. The manual endpoint
is intentionally convenient for a controlled local demo; add access control
before expanding it to a shared environment.

## Build and verification

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux or macOS:

```bash
chmod +x mvnw
./mvnw clean verify
```

This command compiles the application, runs seven integration tests against an
in-memory H2 database, applies the Flyway migration, creates the JAR, produces
the JaCoCo report, and enforces the configured 70% line-coverage gate.

Outputs:

- `target/secureflow-1.0.0.jar`
- `target/surefire-reports/`
- `target/site/jacoco/index.html`

Coverage shows which measured lines executed; assertions determine whether
behavior was correct. Read [testing and evidence](docs/testing.md) for the exact
pass/fail rules, browser command, and verified coverage evidence.

## Request flow

```text
Browser or API client
        |
        v
Spring MVC controller -> service -> repository -> MySQL
                              |
                              +-> monitoring checks -> alerts and history
```

The application packages are `transaction`, `monitoring`, `alert`, `dashboard`,
`demo`, `common`, and `config`. See [architecture](docs/architecture.md) for
component, data-flow, time, transaction, and database details.

## Monitoring defaults

`src/main/resources/application.yml` defines:

```yaml
monitoring:
  amount-limit: 10000.00
  currency: INR
  max-transactions: 5
  window-minutes: 10
```

The current new-payee rule has a clear beginner-friendly definition: the first
account/payee combination in retained data. A configurable cooldown is recorded
as a future extension if the product requirement expands.

## API summary

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/transactions` | Store a transaction and run monitoring |
| `GET` | `/api/transactions` | Filter and page transactions |
| `GET` | `/api/alerts` | Filter and page alerts |
| `GET` | `/api/alerts/{id}` | Read alert details, links, and history |
| `PATCH` | `/api/alerts/{id}/status` | Apply an allowed status transition |
| `GET` | `/api/rules` | Read effective monitoring rules |
| `GET` | `/api/dashboard/summary` | Read all-time aggregate values |
| `POST` | `/api/demo/seed` | Add one synthetic demonstration batch |
| `GET` | `/actuator/health` | Read application health |

See [API reference](docs/api.md) and
[ready-to-run examples](docs/api-examples.http).

## CI and delivery

`.github/workflows/pipeline.yml` runs for pull requests and pushes to `main`.
It separates three outcomes:

1. `test-and-package` runs the seven Spring tests, Flyway, packaging, and the
   JaCoCo threshold, then uploads the JAR and report.
2. `MySQL, Compose and browser checks` starts a disposable MySQL-backed stack,
   verifies health, real persistence, restart persistence, and the non-root
   runtime, then runs Chromium interaction and axe accessibility checks.
3. `Publish container image` runs only after both quality jobs pass on a push
   and publishes `:latest` to GHCR.

The browser dependencies are locked in `package-lock.json`; `npm audit` reported
zero known vulnerabilities for that small test dependency set during the local
2026-08-06 verification.

The current workflow provides continuous integration and registry delivery.
Server restart automation is the next stage; draft PR
[#46](https://github.com/Neueda-Learning/114-Secure-Flow/pull/46) proposes Linux
deployment automation and remains clearly separated from the reviewed `main`
baseline until its environment-specific verification is complete.

## Reviewer documentation

Start with the [documentation index](docs/README.md). Key audit documents are:

- [Project overview](docs/project-overview.md)
- [Requirements](docs/requirements.md)
- [Evidence index](docs/evidence-index.md)
- [Traceability matrix](docs/traceability-matrix.md)
- [Mentor rubric evidence guide](docs/mentor-review-guide.md)
- [Repository review report](docs/review-report.md)
- [Security and threat model](docs/security-and-threat-model.md)
- [India privacy and compliance considerations](docs/privacy-compliance-india.md)
- [Risk register](docs/risk-register.md)
- [AI-assistance record](docs/ai-usage.md)

## Intended use and responsibility

SecureFlow is purpose-built educational software with transparent deterministic
rules and reproducible engineering evidence. Expanding it into a financial,
investigative, compliance, or shared-production context requires the additional
domain, legal, security, and operational validation described in the roadmap.
Project owners and reviewers retain final responsibility for that validation
and for all AI-assisted output.
