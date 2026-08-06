# Development and configuration guide

## Purpose and audience

This guide supports developers running, changing, and diagnosing SecureFlow.
Deployment and operations are covered separately in [deployment](deployment.md).

## Prerequisites

- Java 21
- Git
- internet access for the first Maven dependency download
- optional: Docker Engine/Desktop with Compose v2 for MySQL/container testing
- optional: Node.js 22 for local Playwright tests (CI installs it automatically)

Use the committed Maven Wrapper; a separate Maven installation is unnecessary.
The reviewed wrapper resolves Maven 3.9.16.

## Local test/build workflow

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw clean verify
```

The automated tests use H2 and do not require MySQL. Run the application itself
with MySQL through Docker Compose for the simplest complete environment.

## Run the JAR against MySQL

Provide a reachable database and set:

| Variable | Purpose | Local default in application configuration |
|---|---|---|
| `DB_URL` | JDBC MySQL URL | `jdbc:mysql://localhost:3306/secureflow?...` |
| `DB_USERNAME` | Database application user | `secureflow` |
| `DB_PASSWORD` | Database application password | `secureflow123` |
| `DEMO_SEED_ON_STARTUP` | Seed one batch when database is empty | `false` |

Then:

```bash
./mvnw clean package
java -jar target/secureflow-1.0.0.jar
```

Default credentials are learning conveniences, not production secrets.

## Configuration sources

| File | Scope |
|---|---|
| `src/main/resources/application.yml` | Runtime defaults, monitoring values, health/OpenAPI paths |
| `src/test/resources/application.yml` | H2 test database and test rule values |
| `compose.yaml` | Local MySQL/application services and environment overrides |
| `.env.example` | Example local Compose passwords |
| `pom.xml` | Java, dependencies, build plugins, coverage gate |

Environment variables override the relevant runtime defaults. Monitoring
values currently do not have explicit environment-variable placeholders.

## Database changes

Add a new, immutable migration under
`src/main/resources/db/migration`, for example:

```text
V2__add_reference_index.sql
```

Do not modify an applied migration. Flyway records versions and checksums in
`flyway_schema_history`; an altered migration can stop startup validation.

## Frontend development

The UI is served from `src/main/resources/static` and deliberately avoids a
frontend package manager/build step. Rebuild/restart after source changes and
hard-refresh the browser. Run `npm run test:browser` against the live app, then
manually verify keyboard use, narrow layouts, error states, and affected API
flows. Automated Chromium/axe checks complement manual accessibility and
cross-browser review.

## Useful checks

```bash
./mvnw dependency:tree
docker compose config
curl --fail http://localhost:8080/actuator/health
npm ci
npm run test:browser
```

Manual API requests are in [api-examples.http](api-examples.http). Manual k6
tests are in [load-tests](../load-tests/README.md); they can create many rows and
alerts and should use a disposable environment.

## Troubleshooting

- **Port 8080 in use:** stop the existing process or change the host-side
  Compose port.
- **Database authentication fails after password change:** the named MySQL
  volume retains the original database user/password state. Restore the old
  value or intentionally reset the volume after confirming data loss is safe.
- **Flyway validation fails:** do not bypass it. Compare migration files with
  `flyway_schema_history` and determine whether a migration was changed.
- **Quality checks pass but a later delivery step needs attention:** inspect the
  remaining workflow steps and job permissions. Main run 31098653366 proves
  that the current pipeline can complete its GHCR publication stage.
- **Mojibake/corrupted symbols:** ensure editors use UTF-8. Documentation uses
  plain ASCII diagrams where practical.

## Maintenance

Update this guide whenever environment variables, supported Java/Maven
versions, configuration paths, or development prerequisites change.
