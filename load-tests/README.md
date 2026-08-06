# Manual k6 load and stress tests

## Purpose and boundary

These scripts generate intentional transaction load against an already-running
SecureFlow instance. Their deliberate separation keeps normal builds and demos
fast while enabling controlled performance evidence.

- Maven remains focused on functional integration and coverage.
- Application/Docker startup remains lightweight.
- GitHub Actions avoids unapproved high-volume traffic.
- They create persistent transactions and alerts.
- Run them only against a controlled, disposable test environment.

## Scenarios

### `gradual-ramp.js`

Uses a ramping arrival rate from 5 requests/second toward 200 requests/second
over approximately 3.5 minutes. Script thresholds require:

- HTTP request failure rate below 10%
- p95 request duration below 2 seconds
- p99 request duration below 5 seconds

### `spike-1000-concurrent.js`

Uses 1,000 virtual users and 1,000 shared iterations with a two-minute maximum.
It sends exactly 1,000 iterations if the scenario completes. Thresholds require:

- HTTP request failure rate below 15%
- p95 request duration below 4 seconds
- p99 request duration below 8 seconds

Each payload uses a unique account/payee identifier and may create new-payee
and high-amount alerts. This is not the same as testing one account's velocity.

## Prerequisites

1. SecureFlow and its database are healthy.
2. Docker is installed and can pull `grafana/k6`.
3. The target dataset can be discarded or intentionally retained.
4. Operators have agreed the expected load and monitoring plan.

Current Compose exposes port 8080. The historical script default is 8081, so
**set `BASE_URL` explicitly** for the current project:

```bash
curl --fail http://127.0.0.1:8080/actuator/health
```

## Run using the official container image

From the repository root:

```bash
chmod +x load-tests/run.sh
BASE_URL=http://127.0.0.1:8080 ./load-tests/run.sh gradual
BASE_URL=http://127.0.0.1:8080 ./load-tests/run.sh spike
```

On Docker Desktop, if host networking is unavailable:

```bash
BASE_URL=http://host.docker.internal:8080 ./load-tests/run.sh gradual
BASE_URL=http://host.docker.internal:8080 ./load-tests/run.sh spike
```

`run.sh` pulls the unpinned `grafana/k6` image when absent, mounts the scripts,
tries host networking, and falls back to Docker Desktop's host gateway. For
repeatable evidence, record the resolved k6 image digest/version.

## Pass/fail interpretation

k6 exits non-zero when a configured threshold fails. The response assertion:

```javascript
check(response, {
  "transaction accepted": (r) => r.status === 201,
});
```

records correctness as the `checks` metric. Status results are already visible;
adding an agreed `checks` threshold will make response correctness an automatic
command-level decision alongside the existing HTTP thresholds.

## Evidence collection method

Record:

- commit and application image digest
- k6 version/image digest
- date/time and load-generator host resources
- target environment topology/resources and starting data volume
- exact command and environment variables
- full k6 summary/output
- application/database CPU, memory, connection, and error metrics
- before/after health and row counts
- logs with sensitive data removed
- recovery period and any failed/dropped iterations

Retaining these details promotes an illustrative run into a reproducible,
comparable benchmark.

## Post-run checks

```bash
curl --fail http://127.0.0.1:8080/actuator/health
curl --fail http://127.0.0.1:8080/api/dashboard/summary
curl --fail "http://127.0.0.1:8080/api/transactions?size=20"
curl --fail "http://127.0.0.1:8080/api/alerts?size=20"
```

These checks confirm endpoint response, not data completeness or database
integrity under concurrency.

## Current evidence status

PR [#44](https://github.com/Neueda-Learning/114-Secure-Flow/pull/44)
reports successful gradual and spike executions and supplies both scripts. The
next evidence run should retain raw k6 output, immutable artifacts, environment
specification, and an independent reproduction record.

## Risks and cleanup

- The run can exhaust CPU, memory, DB connections, disk, or logs.
- It can disrupt a shared demo and distort dashboard/chart data.
- `docker compose down --volumes` deletes all local database data; use only
  after confirming the target and accepting data loss.
- Never target a production/shared service without explicit authorization and
  operational safeguards.

## Official reference and maintenance

See [Grafana k6 API load-testing guidance](https://grafana.com/docs/k6/latest/testing-guides/api-load-testing/).
Update this guide and [testing evidence](../docs/testing.md) whenever scenario,
threshold, target port, tooling, or retained evidence changes.
