# API reference

## Purpose, audience, and status

This reference documents endpoints implemented on reviewed `main`. It is for
developers, testers, and controlled-environment operators. Runtime-generated
OpenAPI is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`;
archiving the generated contract is a useful future evidence enhancement.

The API currently uses a trusted-local-operator model. Add authentication and
authorization before expanding the network boundary. Mutating calls are
non-idempotent unless explicitly stated.

## Common conventions

- JSON request/response bodies use UTF-8.
- Transaction and alert times serialize as UTC instants.
- Page numbering is zero-based.
- Page size must be 1-100; default is 20.
- Missing resources and invalid requests use Spring `ProblemDetail`-style JSON.
- Current paths stay simple and unversioned; define `/v1` compatibility rules
  before adding external consumers.

## Transactions

### `POST /api/transactions`

Creates one transaction, executes all monitoring rules, and returns the saved
transaction plus zero or more generated alerts. Success status: `201 Created`.

Request:

```json
{
  "accountId": "ACC-100",
  "payeeId": "PAYEE-100",
  "amount": 250.00,
  "currency": "INR",
  "description": "Monthly service"
}
```

Validation:

| Field | Rules |
|---|---|
| `accountId` | Required; 3-50 characters including surrounding whitespace; letters, numbers, hyphens; trimmed before storage |
| `payeeId` | Same as account ID |
| `amount` | Required; decimal >= 0.01 |
| `currency` | Required; exactly three letters; normalized to uppercase; must equal configured `INR` |
| `description` | Optional; maximum 255 characters; trimmed; blank becomes null |

Clients cannot provide transaction or creation timestamps.

### `GET /api/transactions`

Returns a page sorted by `transactionTime` descending.

| Parameter | Type/default | Behavior |
|---|---|---|
| `search` | optional text | Case-insensitive contains match on account, payee, or description |
| `transactionId` | optional positive integer | Exact database ID |
| `minAmount` | optional decimal | Inclusive lower bound |
| `maxAmount` | optional decimal | Inclusive upper bound |
| `from` | optional ISO instant | Inclusive lower timestamp |
| `to` | optional ISO instant | Inclusive upper timestamp |
| `page` | integer, default 0 | Zero-based page |
| `size` | integer, default 20 | 1-100 |

The service rejects `minAmount > maxAmount` and `from > to`.

## Alerts

### `GET /api/alerts`

Returns alerts sorted by creation time descending.

| Parameter | Allowed values |
|---|---|
| `status` | `OPEN`, `ACKNOWLEDGED`, `INVESTIGATING`, `CLOSED`, `DISMISSED` |
| `severity` | `HIGH`, `MEDIUM`, `LOW` (LOW is defined but no current rule emits it) |
| `page` / `size` | Zero-based; size 1-100 |

Current alert filtering focuses on status, severity, and pagination. The UI
separates current/history views through status groups; text/account/transaction
ID filtering is a clear future API extension.

### `GET /api/alerts/{id}`

Returns the alert's current fields, triggering transactions, and chronological
history. Returns `404` when absent.

### `PATCH /api/alerts/{id}/status`

Request:

```json
{
  "targetStatus": "ACKNOWLEDGED",
  "resolutionNotes": null
}
```

Allowed transitions:

| Current | Next |
|---|---|
| `OPEN` | `ACKNOWLEDGED` |
| `ACKNOWLEDGED` | `INVESTIGATING`, `DISMISSED` |
| `INVESTIGATING` | `CLOSED`, `DISMISSED` |
| `CLOSED`, `DISMISSED` | None |

`CLOSED` and `DISMISSED` require trimmed resolution notes of 3-500 characters.
Invalid transitions return `409 Conflict`; invalid/missing data returns `400`.

## Rules and dashboard

### `GET /api/rules`

Returns the three effective rules and configured values. This is informational;
the endpoint does not edit configuration.

### `GET /api/dashboard/summary`

Returns all-time:

- active alert count (`OPEN`, `ACKNOWLEDGED`, `INVESTIGATING`)
- transaction count
- total alert count
- summed transaction volume

No date filter is applied.

## Demo data

### `POST /api/demo/seed`

Adds one fresh synthetic batch using unique identifiers and the transaction
service. It is non-idempotent and intentionally convenient for a controlled
demo environment. Current test expectations are 20 transactions and 12 alerts
per manual call; protect the endpoint before shared use.

Startup seeding calls a different service method: it skips when at least one
transaction exists.

## Operations and generated documentation

| Endpoint | Purpose | Evidence status |
|---|---|---|
| `GET /actuator/health` | Application/database health | Configured and used by Docker; add a direct contract assertion |
| `GET /actuator/info` | Application info | Exposed with minimal scope; add content assertion if relied upon |
| `GET /v3/api-docs` | Generated OpenAPI JSON | Dependency-provided; archive and contract-test for releases |
| `GET /swagger-ui.html` | Interactive Swagger UI | Configured; add a smoke check when browser automation is introduced |

## Error model

Representative response:

```json
{
  "type": "https://secureflow.local/problems/400",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "errors": {
    "accountId": "must not be blank"
  }
}
```

Expected request failures already use a consistent project handler. Add a
project-specific sanitized fallback and logging policy before shared production
use.

## Examples, evidence, and maintenance

- [Executable HTTP examples](api-examples.http)
- [Controller source](../src/main/java/com/neueda/secureflow/)
- [Integration tests](../src/test/java/com/neueda/secureflow/SecureFlowTest.java)
- [Security boundary](security-and-threat-model.md)

Update this file, examples, requirements, tests, and OpenAPI verification when
an endpoint, field, status, validation rule, or error changes.
