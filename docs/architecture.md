# SecureFlow Engineering Decisions

## Synchronous monitoring

A transaction is saved and evaluated in one service call. A message queue could
improve scale and fault isolation, but it would introduce retry, ordering, and
eventual-consistency concerns that are unnecessary for this MVP. Each rule
implements the same `MonitoringRule` strategy, so asynchronous orchestration can
be introduced later without rewriting the rules.

## Data and time

- Money uses Java `BigDecimal` and MySQL `DECIMAL(19,2)`, never floating point.
- Currency codes are normalized to three uppercase letters.
- API and persistence timestamps use UTC `Instant` values.
- Flyway owns the runtime schema; Hibernate validates rather than creates it.
- Repository indexes support account/time velocity queries and account/payee
  new-payee checks.

## API boundaries

Controllers accept validated request records and return response records instead
of exposing JPA entities. Invalid input returns `400`, missing resources return
`404`, and invalid alert transitions return `409`. Pagination is zero-based and
limited to 100 items per request.

## Alert consistency

Each matching rule creates a separate alert and links the triggering
transactions. Status transitions follow an explicit state machine. Closing or
dismissing requires a resolution note, and each transition appends a history
entry rather than replacing earlier evidence.

## Deployment

The application image is built with Java 21 in a multi-stage Dockerfile and runs
as a non-root user on a smaller JRE image. Docker Compose supplies a private
MySQL 8.4 service, persistent storage, startup ordering, health checks, memory
limits, bounded logs, and restart policies. Only the application port is
published.

## Deliberate MVP limits

Authentication, editable rule administration, multi-currency conversion,
message queues, machine learning, assignment/SLA management, and production
backup automation are outside the classroom scope.
