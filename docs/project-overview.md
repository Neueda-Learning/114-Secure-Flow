# Project overview

## Purpose

SecureFlow demonstrates a small end-to-end transaction-monitoring workflow:
record a payment, run deterministic rules, generate linked alerts, and move an
alert through a review history. It is designed for learning, demonstration,
and engineering assessment rather than real financial operations.

## Scope

### In scope on the reviewed baseline

- INR transaction creation, validation, persistence, filtering, and pagination
- high-amount, transaction-velocity, and first-payee monitoring rules
- alert creation, filtering, detail, linked transactions, and status history
- aggregate dashboard cards and browser-rendered chart exploration
- synthetic demonstration-data generation
- REST/OpenAPI interfaces and a health endpoint
- Maven build, integration tests, Flyway migration, JaCoCo gate, Docker image,
  local Docker Compose, GitHub Actions CI, and attempted GHCR publication

### Deliberately reserved for later maturity stages

- real payment processing or bank integration
- customer identity, login, authorization, or role separation
- case assignment, maker/checker approval, escalation, or regulatory reporting
- machine-learning detection or AI decision-making
- production-grade TLS, secrets, backups, audit-log export, observability, or
  incident automation
- verified legal/regulatory compliance
- verified public or production deployment

## Intended audience

- learners studying Spring Boot, REST, persistence, tests, and containers
- mentors assessing technical breadth, delivery discipline, automation, and
  risk awareness
- developers maintaining the demonstration
- operators running it locally or in a controlled lab

## Current status

The source baseline is `main` commit `9379af1`. Source inspection and persistent
CI evidence verify seven HTTP integration tests, the Flyway V1 migration, the
JaCoCo gate, JAR creation, and Docker image construction. All application
quality stages passed; registry publication is the remaining delivery action
because GHCR rejected the push. See
[EVD-CI-001](evidence-index.md#evd-ci-001-main-quality-gate-and-registry-follow-up).

The proposed review branch adds locally verified MySQL 8.4/Compose runtime and
restart-persistence checks plus two Playwright/axe Chromium checks. These are
recorded separately as [EVD-SYSTEM-001](evidence-index.md#evd-system-001-mysql-compose-and-runtime-verification)
and [EVD-BROWSER-001](evidence-index.md#evd-browser-001-browser-and-automated-accessibility-verification)
until published CI independently reproduces them.

Draft PR [#46](https://github.com/Neueda-Learning/114-Secure-Flow/pull/46)
proposes Linux continuous deployment. PR
[#47](https://github.com/Neueda-Learning/114-Secure-Flow/pull/47) proposes chart
presentation changes. Neither is part of this baseline.

## Stakeholders and responsibilities

| Role | Responsibility |
|---|---|
| Project owner | Confirms requirements, accepts risks, approves releases and claims |
| Contributor | Implements a scoped change and records truthful evidence |
| Reviewer | Compares claims with diff, source, tests, CI, risks, and requirements |
| Operator | Protects credentials/data and follows deployment/backup controls |
| Mentor/evaluator | Assesses repository evidence within its stated limitations |

Git history and issue assignments provide useful contribution evidence. A
future ownership table can formalize ongoing support and release responsibility.

## Success criteria

For the learning scope, success means the documented flows behave as specified,
the reproducible build passes, and evidence plus boundaries are traceable.
Financial-control effectiveness, formal assurance, compliance, production, and
capacity claims become available only after their specialist validation stages.

## Assumptions

- Test and demo data are synthetic and contain no real personal or financial
  information.
- A trusted learner/operator controls the local environment.
- INR is the only accepted currency in current business logic.
- The database is a single MySQL instance; tests substitute H2 in MySQL mode.
- Repository-hosted CI and linked GitHub evidence remain accessible.

## Improvement roadmap

The project has a clear path from controlled learning use to greater operational
maturity: identity/access control, managed credentials, protected demo actions,
retention and recovery, automated dependency/security analysis, broader manual
accessibility/compatibility assurance, and registry-permission alignment. Priorities and proposed
owners are in the [risk register](risk-register.md).

## Related documents

- [Requirements](requirements.md)
- [Architecture](architecture.md)
- [Testing](testing.md)
- [Mentor rubric evidence guide](mentor-review-guide.md)
- [Security and threat model](security-and-threat-model.md)
- [Current boundaries and improvement roadmap](known-limitations.md)
- [Future scope](future-scope.md)

## Maintenance

Update this overview when the product boundary, intended users, supported
environment, or production status changes. Do not move planned capabilities
into the implemented scope until merged source and verification evidence exist.
