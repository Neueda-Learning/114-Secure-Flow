# Architecture

SecureFlow is a single Spring Boot application with a static browser dashboard
and a MySQL database.

## Runtime components

~~~text
Browser
  │ HTTP and JSON
  ▼
Spring Boot application
  ├── transaction endpoints
  ├── monitoring checks
  ├── alert endpoints
  ├── dashboard summary
  └── static HTML/CSS/JavaScript
  │ JPA
  ▼
MySQL
~~~

Docker Compose starts MySQL first. After the database health check succeeds, it
starts the application.

## Create-transaction flow

~~~text
POST /api/transactions
  ↓
TransactionController validates JSON shape
  ↓
TransactionService trims IDs and confirms INR
  ↓
TransactionRepository checks whether the payee is new
  ↓
TransactionRepository saves the transaction with server time
  ↓
MonitoringService runs amount, velocity, and new-payee checks
  ↓
AlertService saves every matching alert and initial history entry
  ↓
API returns the saved transaction and generated alerts
~~~

The transaction and generated alerts are created in one Spring transaction. If
a database operation fails, the whole operation rolls back.

## Monitoring rules

All three rules are in **MonitoringService**.

### Amount

~~~java
if (transaction.getAmount().compareTo(rules.amountLimit()) > 0) {
    // create HIGH amount alert
}
~~~

The boundary is strictly greater than the configured limit.

### Velocity

The repository loads transactions for the same account between:

~~~text
transaction time - configured window
and
transaction time
~~~

An alert is created when the number of transactions is greater than the
configured maximum.

### New payee

Before saving a transaction, the repository checks whether the account has ever
used that payee. No previous match means the new-payee rule creates a MEDIUM
alert.

## Alert status flow

~~~text
OPEN
  ↓
ACKNOWLEDGED
  ├──→ DISMISSED
  ↓
INVESTIGATING
  ├──→ DISMISSED
  ↓
CLOSED
~~~

CLOSED and DISMISSED are final. Closing or dismissing requires resolution notes
of at least three characters.

Every change creates an **alert_status_history** row.

## Database tables

| Table | Purpose |
|---|---|
| transactions | Stored INR payments |
| alerts | Rule matches and current investigation status |
| alert_transactions | Links an alert to one or more transactions |
| alert_status_history | Audit trail for alert status changes |
| flyway_schema_history | Flyway's record of applied migrations |

The schema is created by **V1__create_tables.sql**. Production uses MySQL. Tests
use H2 in MySQL compatibility mode and run the same Flyway migration.

## Package responsibilities

| Package | Responsibility |
|---|---|
| transaction | Transaction API, persistence, search, and DTOs |
| monitoring | Three checks and rule-definition endpoint |
| alert | Alert persistence, lifecycle, detail, and DTOs |
| dashboard | IST-day summary calculations |
| common | API error format and page response |
| config | Typed monitoring configuration |

## Time handling

Transactions use **Instant.now()**, which represents an unambiguous UTC instant.
The database and JSON handling use UTC. The browser formats timestamps in
Asia/Kolkata.

Dashboard "today" is calculated from midnight to midnight in Asia/Kolkata and
then converted to UTC instants for database queries.

## Deliberate simplifications

The application avoids separate mapper classes, rule strategy classes, criteria
specification classes, and mock-heavy unit tests.

Response records contain their own small conversion methods. Repository queries
handle filtering. Monitoring logic stays in one service so a learner can follow
the complete decision path.
