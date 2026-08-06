# Mentor rubric evidence guide

## Purpose and audience

This guide gives mentors, evaluators, and project contributors the shortest
honest route through SecureFlow's strongest repository evidence. It complements
the detailed [traceability matrix](traceability-matrix.md) and does not assign a
rating on the evaluator's behalf.

Baseline: reviewed `main` commit `9379af1`, with final branch validation recorded
on 2026-08-06. Proposed-branch evidence is clearly separated from published
`main` evidence.

## Evidence-led summary

| Rubric area | Strongest repository evidence | What it demonstrates |
|---|---|---|
| Technical proficiency and dual-skilling | [Technology inventory](technology-inventory.md), [architecture](architecture.md), Java/JavaScript/SQL/YAML/Shell source | Breadth across backend, browser UI, database, tests, containers, and CI plus detailed understanding of their interaction |
| Solution design and implementation | [Requirements](requirements.md), [API](api.md), [ADRs](decisions/README.md), [traceability](traceability-matrix.md) | End-to-end flow, explicit design choices, acceptance evidence, and maintainable component boundaries |
| Automation and modernization | Maven Wrapper, Flyway, JaCoCo, Docker/Compose, GitHub Actions, demo seeding, k6 | Repeatable build, migration, testing, coverage, packaging, demonstration, and controlled load generation |
| Compliance, security, and risk awareness | [Threat model](security-and-threat-model.md), [India considerations](privacy-compliance-india.md), [risk register](risk-register.md), [security policy](../SECURITY.md) | Implemented safeguards, proactive issue identification, prioritized solutions, responsible legal qualification, and maintenance ownership |
| Collaboration and delivery discipline | [Repository history](repository-history.md), issues, PRs, templates, [workflow](repository-workflow.md) | Multi-contributor delivery, scoped branches/PRs, CI feedback, authentic authorship, and an improved future review process |
| Evidence quality and integrity | [Evidence index](evidence-index.md), [review report](review-report.md), [AI record](ai-usage.md) | Reproducible claims, persistent links, transparent boundaries, AI disclosure, and preserved history |

## Recommended 10-minute repository walkthrough

### 1. Establish scope and current success

Open the [README](../README.md) and explain:

- the educational transaction-monitoring purpose
- the implemented transaction, alert, dashboard, chart, pagination, and demo
  capabilities
- the seven-test Maven quality gate, 70% JaCoCo threshold, and 97.09% local report
- the MySQL/Compose runtime, persistence, Chromium, and axe checks
- the successful JAR and Docker construction stages
- the clearly isolated GHCR permission follow-up

Use [`EVD-CI-001`](evidence-index.md#evd-ci-001-main-quality-gate-and-registry-follow-up)
instead of relying on memory.

### 2. Trace one feature end to end

Use transaction creation as the example:

```text
Browser form
  -> POST /api/transactions
  -> request validation
  -> TransactionService normalization and timestamp
  -> MonitoringService rules
  -> transaction/alert/history persistence
  -> API response
  -> dashboard and alert UI refresh
```

Then show:

- [`TransactionController`](../src/main/java/com/neueda/secureflow/transaction/TransactionController.java)
- [`TransactionService`](../src/main/java/com/neueda/secureflow/transaction/TransactionService.java)
- [`MonitoringService`](../src/main/java/com/neueda/secureflow/monitoring/MonitoringService.java)
- [`V1__create_tables.sql`](../src/main/resources/db/migration/V1__create_tables.sql)
- [`SecureFlowTest`](../src/test/java/com/neueda/secureflow/SecureFlowTest.java)

This single trace demonstrates requirements translation, layered design,
validation, persistence, monitoring, UI integration, and testing.

### 3. Explain the quality gate

Show the command rather than describing it vaguely:

```powershell
.\mvnw.cmd --batch-mode clean verify
```

Explain the decision sequence:

```text
compile -> start Spring/H2 -> Flyway V1 -> 7 HTTP integration tests
        -> executable JAR -> JaCoCo report -> 70% coverage check
```

The documented local result is `EVD-DOC-001`: 7 tests, 0 failures, 0 errors,
0 skipped, successful Flyway migration, JAR creation, JaCoCo gate, and 97.09%
reported line coverage. If the
command is rerun for a presentation, report that new result separately rather
than replacing the recorded result.

### 4. Show automation as a connected system

Use the following sequence:

```text
Developer change
  -> pull request
  -> Maven tests and JaCoCo
  -> JAR artifact
  -> MySQL/Compose/API/non-root/restart checks
  -> Chromium journey and axe scan
  -> multi-stage non-root Docker image
  -> registry delivery
  -> future supervised Linux deployment
```

Reference [testing](testing.md), [deployment](deployment.md), and
[repository workflow](repository-workflow.md). Emphasize that Flyway removes
manual schema setup, demo seeding removes repetitive presentation data entry,
and Compose starts the application/database in a repeatable order.

### 5. Demonstrate proactive security and compliance thinking

Show implemented safeguards first:

- server-side validation
- controlled alert-state transitions
- loopback-only default host binding
- non-root application container
- non-exposed MySQL host port
- Flyway/Hibernate schema validation
- synthetic demo-data boundary
- private vulnerability-reporting guidance

Then show how every next-stage control has a solution path in the
[risk register](risk-register.md). This demonstrates proactive risk management,
not merely awareness of terminology.

### 6. Show collaboration without changing history

Use [repository history](repository-history.md) to identify actual issues, PRs,
authors, outcomes, CI states, and open work. Each contributor should select a PR
they actually authored and explain:

1. the problem and acceptance criteria
2. their implementation decision
3. feedback or CI signal received
4. how they verified the result
5. what they would improve next

This supplies the human evidence needed for individual ownership, growth
mindset, learning, feedback, and dual-skilling criteria.

## Rubric-specific reviewer notes

### Technical proficiency and dual-skilling

Direct artifacts cover Java 21, Spring Boot, REST, validation, JPA, MySQL,
Flyway, HTML/CSS/JavaScript, charts, JUnit/MockMvc/H2, JaCoCo, Maven, Docker,
Compose, GitHub Actions, Shell, and k6. The architecture, API, test, deployment,
and technology documents demonstrate how the technologies cooperate rather than
presenting them as an isolated list.

For individual scoring, each contributor should explain one primary contribution
and one unfamiliar technology they learned. The repository cannot replace that
personal explanation.

### Solution design and implementation

The repository maps stable requirement IDs to controllers, services,
repositories, migrations, browser behavior, tests, issues, PRs, and evidence.
Three ADRs document present design rationale and trade-offs without inventing
historical decisions. The application provides a complete browser-to-database
flow with alert lifecycle history and reproducible demonstration data.

### Automation and modernization

Automation has practical, visible outcomes:

- one Maven command compiles, tests, packages, reports coverage, and enforces a
  quality threshold
- Flyway applies versioned schema SQL automatically
- Compose coordinates MySQL/application health and persistence, with executable
  smoke and restart checks
- Playwright/axe automate the primary live browser journey and common WCAG checks
- GitHub Actions separates Maven quality, system verification, and delivery
- demo seeding creates current-time presentation data through real services
- k6 supplies controlled gradual and 1,000-user scenarios
- AI assistance accelerated review and documentation under human responsibility

Future business-value measurements can quantify time saved without weakening
the existing automation evidence.

### Compliance, security, and risk awareness

The security documentation separates implemented safeguards from the shared-
environment maturity plan. The India assessment links authoritative sources,
uses qualified language, keeps data synthetic, and converts each legal/privacy
topic into an engineering control objective. The 24-item risk register pairs
every concern with an existing safeguard and solution path.

## Precise claim language

Use:

- “Seven integration tests passed for commit `9379af1` and in the documented
  local regression run.”
- “The configured JaCoCo 70% line-coverage gate passed.”
- “The application quality and Docker construction stages passed; GHCR
  permissions are the isolated delivery follow-up.”
- “The default Compose configuration is designed for controlled local use.”
- “The repository documents a security and compliance maturity roadmap.”

Reserve formal production, security, performance, accessibility, legal, and
regulatory claims until their specialist evidence exists. Precise language
strengthens credibility and protects the verified achievements.

## Presentation-readiness checklist

- [ ] Confirm each contributor's chosen issue/PR and personal explanation.
- [ ] Rehearse the browser-to-database transaction trace.
- [ ] Rehearse simple explanations of Flyway, JaCoCo, JAR, Docker, CI/CD, and k6.
- [ ] Decide whether to run `clean verify` live or show the persistent CI link.
- [ ] Confirm demo data is synthetic and startup/manual seeding behavior is understood.
- [ ] Show one valid and one rejected alert transition.
- [ ] Show requirement-to-test-to-evidence traceability.
- [ ] Show implemented security safeguards before the maturity roadmap.
- [ ] Confirm the latest GitHub PR/CI state immediately before presentation.
- [ ] Record any newly executed evidence with its commit, command, environment,
      result, and limitations.

## Related documents

- [Documentation index](README.md)
- [Traceability matrix](traceability-matrix.md)
- [Evidence index](evidence-index.md)
- [Repository review report](review-report.md)
- [AI-assistance record](ai-usage.md)

## Maintenance

Update this guide when the baseline, rubric source, requirements, tests, CI,
deployment state, security controls, or contributor evidence changes. Keep the
guide short enough to use during a real review and link detailed proof rather
than duplicating it.
