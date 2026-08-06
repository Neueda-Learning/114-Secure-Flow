# Architecture decision records

## Purpose

These records explain important current design choices and their consequences.
The original historical rationale was not fully documented, so the records are
labelled as **current reconstructions**, not invented historical decisions.

| ADR | Decision | Status |
|---|---|---|
| [ADR-001](ADR-001-modular-monolith-and-static-ui.md) | Keep one Spring Boot deployable with a static browser UI | Accepted as current reconstruction |
| [ADR-002](ADR-002-flyway-and-hibernate-validation.md) | Use Flyway for schema evolution and Hibernate `validate` | Accepted as current reconstruction |
| [ADR-003](ADR-003-integration-tests-with-h2.md) | Keep fast H2 integration tests and complement them with a MySQL/Compose system check | Accepted with layered parity evidence |

## Maintenance

Create a new ADR when a material architecture, data, security, or deployment
choice changes. Do not silently rewrite an accepted record to make a new choice
look historical; supersede it and link both decisions.
