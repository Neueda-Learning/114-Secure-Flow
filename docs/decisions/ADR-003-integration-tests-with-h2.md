# ADR-003: Spring integration tests with H2 in MySQL mode

- Status: Accepted for fast feedback with an implemented Compose/MySQL system layer
- Review condition: Revisit when database behavior, migrations, performance,
  or production assurance requires real-MySQL verification.

## Context and problem

Developers need fast, repeatable HTTP-to-database tests without requiring a
local MySQL service for every Maven build.

## Considered options

1. Spring Boot + MockMvc + in-memory H2 in MySQL mode.
2. Isolated unit tests with mocks only.
3. MySQL/Testcontainers integration tests.
4. A shared external test database.

The original rationale is not recorded; this is reconstructed from the suite.

## Decision and rationale

Use one readable Spring Boot integration-test class with MockMvc and H2. It
exercises controllers, services, repositories, validation, and the Flyway SQL
without an external database.

## Benefits and trade-offs

- Fast developer onboarding and one-command verification.
- Strong component integration coverage for seven scenarios.
- Full-context failures provide strong integration signals; focused unit tests
  can make complex rule diagnosis even faster.
- H2 supplies fast compatibility feedback, while the Compose system job supplies
  MySQL 8.4 migration, API, persistence, and runtime verification.
- Network, browser, and container checks are complementary external layers rather
  than part of this fast Maven suite.

## Maturity safeguards

- Keep H2 tests for fast feedback.
- Keep the separate MySQL/Compose verification scripts and CI job green.
- Add focused Testcontainers repository cases if direct SQL or migration
  complexity creates gaps not covered by the system smoke.
- Add focused unit tests for complex rule boundaries as behavior grows.
- Treat CI's H2/Flyway version warning as a compatibility signal to review.

## Implementation and evidence

- `src/test/java/com/neueda/secureflow/SecureFlowTest.java`
- `src/test/resources/application.yml`
- [Testing](../testing.md)
- [EVD-CI-001](../evidence-index.md#evd-ci-001-main-quality-gate-and-registry-follow-up)
- [EVD-SYSTEM-001](../evidence-index.md#evd-system-001-mysql-compose-and-runtime-verification)
