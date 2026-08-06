# ADR-001: Modular monolith with a static browser UI

- Status: Accepted as current reconstruction
- Review condition: Revisit if independent scaling, separate release cadence,
  complex frontend tooling, or external consumers require stronger boundaries.

## Context and problem

SecureFlow is a small learning application whose transaction, monitoring,
alert, dashboard, and demo flows are tightly related. It needs a deployable UI
and API without a large operational/tooling burden.

## Considered options

1. One Spring Boot application serving API and static UI.
2. Separate backend and JavaScript frontend builds.
3. Multiple services for transaction, monitoring, and alert domains.

The original decision discussion is not available. This comparison is a current
reconstruction from the implementation.

## Decision and rationale

Keep one Spring Boot deployable, organized into feature packages, with static
HTML/CSS/JavaScript. This matches current size, enables one build/container, and
keeps the end-to-end flow approachable.

## Benefits and trade-offs

- Simple build, local run, deployment, debugging, and data transactions.
- Minimal frontend dependency/build-chain overhead.
- Frontend/backend release and scaling remain intentionally coupled at the
  current project size.
- Feature-package conventions keep boundaries approachable; enforce them through
  review until independent deployment becomes valuable.
- Browser automation and additional frontend tooling can be added explicitly as
  requirements grow.

## Maturity safeguards

- Growing services can become tightly coupled; preserve package responsibilities
  and record cross-boundary changes.
- Dynamic HTML can introduce XSS; retain escaping and add security tests.
- A single runtime is a single availability boundary; add operational controls
  before production use.

## Implementation and evidence

- `src/main/java/com/neueda/secureflow/`
- `src/main/resources/static/`
- `Dockerfile`
- [Architecture](../architecture.md)
