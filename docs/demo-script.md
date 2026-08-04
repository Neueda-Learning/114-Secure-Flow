# SecureFlow Demo Script (18 Minutes)

## 0:00–2:00 — Person 1: problem and process

- Introduce the four-person team.
- Explain suspicious transaction monitoring in one sentence.
- Show the GitHub Project and feature-branch/PR workflow.
- State the current release boundary and why the team chose a focused design.

## 2:00–5:00 — Person 2: transactions

- Explain the transaction fields and why money uses `BigDecimal`.
- Add one normal transaction.
- Search/filter it in the transaction ledger.
- Explain MySQL persistence and Flyway.

## 5:00–8:00 — Person 3: rules

- Show the read-only Rules screen.
- Explain the Strategy interface in plain language: every rule receives the same transaction/context and may return a match.
- Trigger high-amount and velocity scenarios.
- Explain the exact boundary tests.

## 8:00–12:00 — Person 4: alert operations

- Open an alert and show linked transactions.
- Acknowledge, start investigation, add notes, and close it.
- Show the audit timeline and alert history.
- Resize the browser briefly to show responsive design.

## 12:00–15:00 — Person 1: quality and CI/CD

- Show a green `clean verify`, the coverage percentage, and one test.
- Show GitHub Actions: PR CI, MySQL/Flyway smoke test, JAR, and GHCR image.
- Show the tested Linux Compose deployment and explain its localhost security boundary.

## 15:00–18:00 — Everyone: learning and next steps

- Each person states one challenge and one lesson.
- Mention possible next work: daily limit, editable rules, deduplication, async processing, authentication.
- Invite questions.

## Backup plan

- Rehearse with an empty database.
- Keep screenshots of Overview, an alert timeline, Swagger, and a green workflow.
- If live data creation fails, show Swagger responses and explain the error honestly.
- Never hide a failure or edit database rows manually during the demo.
