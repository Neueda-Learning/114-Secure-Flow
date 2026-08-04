# Five-Minute Demonstration

## Preparation

- Confirm `docker compose ps` shows both containers as healthy.
- Open the dashboard, Swagger UI, GitHub Actions, and repository documentation
  in separate tabs.
- Use fresh account IDs so the new-payee scenario is deterministic.

## Script

1. **Problem and dashboard (30 seconds):** explain that SecureFlow records
   transactions, evaluates transparent rules, and gives analysts an auditable
   investigation queue.
2. **New payee (45 seconds):** submit the first payment from `DEMO-001` to
   `PAYEE-NEW`; show the medium-severity new-payee alert.
3. **High amount (30 seconds):** submit ₹15,000 for the same pair; show the
   high-severity amount alert and explain the strict ₹10,000 boundary.
4. **Velocity (45 seconds):** submit enough payments for `DEMO-VELOCITY` to
   reach six within ten minutes; show the high-severity velocity alert.
5. **Investigation (60 seconds):** acknowledge an open alert, start
   investigation, close it with a meaningful resolution note, and show its
   history and linked transactions.
6. **Search and rules (30 seconds):** filter the transaction table for `DEMO`
   and show the read-only effective rule configuration.
7. **Engineering evidence (45 seconds):** show Swagger, the architecture
   diagram, a green CI run, coverage artifact, Docker Compose health, and the
   published container workflow.
8. **Close (15 seconds):** summarize: save transaction, run rules, create
   alerts, investigate, retain the audit trail.

If external internet access fails, the locally deployed dashboard, Swagger UI,
health endpoint, and `docker compose ps` output are sufficient backup evidence.
