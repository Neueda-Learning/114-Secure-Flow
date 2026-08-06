# Deployment and operations guide

## Purpose, audience, and current boundary

This guide covers direct Docker Compose operation. The private Amazon Linux
server's pull-based systemd deployment is documented separately in
[automatic Linux deployment](continuous-deployment.md).

## Requirements

- Docker Engine or Docker Desktop
- Docker Compose v2 with `--wait` support
- internet access on first image/dependency pulls
- approximately 2 GB available memory (estimate; not benchmarked)

Java, Maven, and MySQL do not need to be installed on the host.

## Configuration and secrets

Copy the local example:

```bash
cp .env.example .env
```

```powershell
Copy-Item .env.example .env
```

Change both values before a shared environment. `.env` is ignored by Git, but
ignore rules do not make weak or leaked credentials safe.

| Variable | Used by | Purpose |
|---|---|---|
| `DB_PASSWORD` | MySQL and application | Application database user's password |
| `DB_ROOT_PASSWORD` | MySQL | Database administrative password |

The checked-in fallback passwords are only for local learning. There is no
managed secret integration on `main`.

## Validate and start

```bash
docker compose config
docker compose up --build --wait
```

Expected sequence:

1. Compose creates its network and `mysql-data` named volume.
2. MySQL 8.4 starts and passes `mysqladmin ping`.
3. Docker builds the JAR in a Maven/Java 21 build stage with tests skipped.
4. The JAR is copied into a smaller Java 21 runtime image.
5. The application runs as the non-root `secureflow` user.
6. Flyway applies pending migrations; Hibernate validates mappings.
7. Startup demo seeding runs only when configured and data is empty.
8. Docker checks `/actuator/health` until healthy or retries are exhausted.

Open <http://localhost:8080>. The host bind is `127.0.0.1`, so other machines
cannot connect directly using the default mapping.

## Windows source with WSL Docker

Run Compose from a WSL shell against the Windows-mounted repository, for
example:

```bash
cd /mnt/c/Users/btw/Desktop/SecureFlow
docker compose up --build --wait
```

For faster and more reliable Linux filesystem performance, a WSL-native clone
can be preferable. Do not maintain two uncontrolled copies; choose one working
copy and use Git for synchronization.

## Health, status, and logs

```bash
docker compose ps
curl --fail http://localhost:8080/actuator/health
docker compose logs --follow app
docker compose logs --follow database
```

A healthy response normally contains `{"status":"UP"}`. Health confirms the
configured health contributors respond; it is not a complete business or
security test.

## Demo operation

Compose sets `DEMO_SEED_ON_STARTUP=true`. To add a fresh batch immediately
before a presentation:

```bash
curl -X POST http://localhost:8080/api/demo/seed
```

This mutates the database. Repeated use accumulates data and alerts.

## Stop, restart, and data persistence

Keep data:

```bash
docker compose down
docker compose up --build --wait
```

Delete all application database data:

```bash
docker compose down --volumes
```

The second command is destructive and should be used only after confirming the
target Compose project and accepting data loss. Automated backup and restore
are documented as the next data-protection layer.

## Orphan-container warning

If Compose reports an orphan such as an older `db`/`database` service, first
inspect:

```bash
docker compose ps --all
docker ps --filter label=com.docker.compose.project=secureflow
```

Then, only for the verified SecureFlow Compose project:

```bash
docker compose down --remove-orphans
docker compose up --build --wait
```

This removes project containers, not the named volume unless `--volumes` is
also supplied.

## Reproducible runtime verification

The CI system job starts a disposable stack and runs:

```bash
bash scripts/verify-running-app.sh
bash scripts/verify-volume-persistence.sh
```

The first script requires application health `UP`, creates and searches a real
transaction, confirms at least one MySQL row, and rejects a root application
UID. The second records the transaction count, performs a Compose stop/start
without deleting the named volume, and requires the same count afterward.
CI removes the disposable volume in its `always()` cleanup step.

For an isolated local run beside a development stack, choose a different
project name and port:

```bash
COMPOSE_PROJECT_NAME=secureflow-smoke APP_PORT=18080 \
  docker compose up --build --detach --wait
APP_BASE_URL=http://127.0.0.1:18080 \
  COMPOSE_PROJECT_NAME=secureflow-smoke APP_PORT=18080 \
  bash scripts/verify-running-app.sh
COMPOSE_PROJECT_NAME=secureflow-smoke APP_PORT=18080 \
  bash scripts/verify-volume-persistence.sh
COMPOSE_PROJECT_NAME=secureflow-smoke APP_PORT=18080 \
  docker compose down --volumes --remove-orphans
```

## Published image status

The workflow intends to publish:

```text
ghcr.io/neueda-learning/114-secure-flow:latest
ghcr.io/neueda-learning/114-secure-flow:sha-<40-character-commit-SHA>
```

The reviewed `main` run on 2026-08-06 successfully published `:latest`. The
package is currently private: an anonymous manifest request returns HTTP 401.
The workflow grants `packages: write` only to the publication job and signs in
with `GITHUB_TOKEN`. The Linux deployment prefers the immutable SHA tag when
the deployment user has `read:packages` access, then falls back to building the
same tested source SHA if the pull is unavailable. See GitHub's
[package-permissions guidance](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages).

The image contains only the application and still needs MySQL plus `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD`.

## Troubleshooting and recovery

### MySQL remains unhealthy

```bash
docker compose logs database
```

If passwords changed after volume creation, the stored database account does
not automatically adopt new environment values. Restore the correct value or
reset only after accepting data loss.

### Application remains unhealthy

```bash
docker compose logs app
```

Check database connectivity, Flyway validation, entity/schema mismatch, port,
and memory failures.

### Rebuild without cache

```bash
docker compose build --no-cache app
docker compose up --wait
```

### Delivery step needs attention

Inspect the specific GitHub Actions step. Stage-level evidence distinguishes a
successful Maven quality result from image publication and deployment results.

## Shared-environment readiness checklist

Before any production/shared deployment, owners must design and verify at least:

- authentication, authorization, and privileged demo endpoint controls
- HTTPS/reverse proxy and network/firewall rules
- managed secrets and credential rotation
- immutable image identity, SBOM, signature/provenance, and vulnerability scan
- resource limits, logs, metrics, alerting, and incident ownership
- MySQL hardening, encryption, retention, backup, restore, and disaster recovery
- data/privacy/legal review and access/audit requirements
- staged deployment, rollback, change approval, and supervised first release

## Maintenance

Keep this document aligned with `Dockerfile`, `compose.yaml`, runtime variables,
health checks, and actual registry/deployment evidence. Never describe a draft
PR as deployed.
