# Manual Stress Tests (Separate From Normal Tests)

This folder contains **manual-only** load tests for transaction ingestion.

- It is not part of Maven test lifecycle.
- It is not part of Docker startup.
- It is not part of CI workflows.

## Scenarios

1. `gradual-ramp.js`
- Gradually increases request rate to simulate progressive load.

2. `spike-1000-concurrent.js`
- Sends 1000 requests with 1000 virtual users to simulate an instant spike.

## Prerequisites

1. SecureFlow app must already be running.
2. Health endpoint must be UP.
3. Docker must be installed (already required for this project). No k6 install needed.

Quick check:

```bash
curl -s http://127.0.0.1:8081/actuator/health
```

## Run Tests (One Command, No Install)

From the repository root, run once to make the script executable:

```bash
chmod +x load-tests/run.sh
```

Then run either scenario:

```bash
./load-tests/run.sh gradual
./load-tests/run.sh spike
```

`run.sh` automatically:
- pulls the official `grafana/k6` image if it isn't cached yet
- fixes local file permissions so the container can read the scripts
- uses `--network host` on Linux, or `host.docker.internal` on Docker Desktop
- defaults to `http://127.0.0.1:8081`; override with `BASE_URL=http://your-host:port ./load-tests/run.sh gradual`

### Optional: local k6 install instead of Docker

If you prefer a local binary: `winget install k6.k6` (Windows) or follow the
official k6 install docs, then run `k6 run load-tests/gradual-ramp.js` directly.

## Suggested Execution Flow

1. Start SecureFlow stack.
2. Confirm app health is UP.
3. Run `gradual-ramp.js` first.
4. Let system recover for 1-2 minutes.
5. Run `spike-1000-concurrent.js`.
6. Collect metrics and logs.
7. Verify app still healthy.

## Post-Run Verification

```bash
curl -s http://127.0.0.1:8081/actuator/health
curl -s http://127.0.0.1:8081/api/dashboard/summary
curl -s "http://127.0.0.1:8081/api/transactions?size=20"
curl -s "http://127.0.0.1:8081/api/alerts?size=20"
```

## Keep It Separate

- Do not call these scripts from CI.
- Do not call these scripts from startup scripts.
- Run only when you intentionally execute the commands above.
