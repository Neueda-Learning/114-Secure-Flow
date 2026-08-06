# ADR-002: Flyway migrations with Hibernate schema validation

- Status: Accepted as current reconstruction
- Review condition: Revisit if database ownership, deployment order, rollback,
  or multi-service schema requirements change.

## Context and problem

The application needs a repeatable schema and must avoid Hibernate silently
changing production tables from entity definitions.

## Considered options

1. Flyway versioned SQL plus Hibernate `ddl-auto: validate`.
2. Hibernate automatic create/update.
3. Manually administered SQL outside application version control.

The original discussion is not available; options are reconstructed from the
current configuration.

## Decision and rationale

Flyway applies versioned SQL and records checksums/history. Hibernate validates
that entity mappings match the resulting schema. This makes schema changes
reviewable and repeatable while preventing implicit ORM changes.

## Consequences

- Every schema change requires a new immutable migration.
- Startup can fail early when migrations or mappings disagree.
- Rollback requires an explicit recovery plan; MySQL DDL may not be fully
  transactional.
- The application database user needs migration privileges at startup.

## Maturity safeguards

- Editing an applied migration breaks checksum trust: add a new version.
- MySQL migration tests and documented recovery make DDL changes safe to operate
  before production use.
- H2 compatibility is incomplete: add MySQL integration tests.

## Implementation and evidence

- `src/main/resources/db/migration/V1__create_tables.sql`
- `src/main/resources/application.yml`
- `pom.xml`
- [Database architecture](../architecture.md#database-design)
