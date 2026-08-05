# SecureFlow

[![CI and CD](https://github.com/Neueda-Learning/114-Secure-Flow/actions/workflows/pipeline.yml/badge.svg)](https://github.com/Neueda-Learning/114-Secure-Flow/actions/workflows/pipeline.yml)

SecureFlow is a small transaction-monitoring application built with Java 21,
Spring Boot, MySQL, Flyway, Maven, JaCoCo, Docker, and plain HTML/CSS/JavaScript.

The code is intentionally direct and readable. A transaction is saved, three
monitoring checks run immediately, and matching alerts are stored with their
status history.

## Features

- Store and search INR transactions.
- Use server time for new transactions and display timestamps in IST.
- Create a HIGH alert when an amount is above INR 10,000.
- Create a HIGH alert after more than five transactions in ten minutes.
- Create a MEDIUM alert when an account uses a payee for the first time.
- Support OPEN → ACKNOWLEDGED → INVESTIGATING → CLOSED.
- Support dismissal from ACKNOWLEDGED or INVESTIGATING.
- Keep the transactions and history linked to each alert.
- Provide a responsive dashboard, REST API, Swagger UI, and health endpoint.
- Persist production data in MySQL.
- Verify the application with end-to-end tests and a 70% JaCoCo gate.

## Quick start with Docker

Requirements:

- Docker Engine or Docker Desktop
- Docker Compose

Start the application and MySQL:

~~~bash
docker compose up --build
~~~

Open:

- Dashboard: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Stop the containers and keep the database:

~~~bash
docker compose down
~~~

Delete the containers and all saved database data:

~~~bash
docker compose down --volumes
~~~

The included passwords are for local learning only. Copy **.env.example** to
**.env** and change the values before sharing a deployment. Port 8080 is bound
to 127.0.0.1, so the default Compose setup is accessible only from the same
computer.

## Run the quality gate locally

Requirements:

- Java 21
- Internet access on the first build

Windows:

~~~powershell
.\mvnw.cmd clean verify
~~~

Linux or macOS:

~~~bash
chmod +x mvnw
./mvnw clean verify
~~~

This one command:

1. removes the previous build
2. compiles the Java source
3. starts the test application
4. applies the Flyway migration to the test database
5. runs six HTTP-level tests
6. creates the runnable JAR
7. creates the JaCoCo HTML report
8. fails if measured line coverage is below 70%

Outputs:

- JAR: **target/secureflow-1.0.0.jar**
- Coverage report: **target/site/jacoco/index.html**
- Test reports: **target/surefire-reports/**

## Simple request flow

~~~text
Browser
  ↓
Controller receives JSON
  ↓
Service validates and saves data
  ↓
MonitoringService runs three if statements
  ↓
AlertService saves matching alerts
  ↓
Repository reads or writes the database
~~~

The main packages are:

- **transaction** — transaction endpoint, validation, persistence, and search
- **monitoring** — amount, velocity, and new-payee checks
- **alert** — alert creation, filtering, detail, and status changes
- **dashboard** — summary counts and transaction volume
- **common** — paging and consistent API errors
- **config** — monitoring values loaded from application configuration

See [Architecture](docs/architecture.md) for the complete code and data flow.

## Monitoring configuration

The defaults are in **src/main/resources/application.yml**:

~~~yaml
monitoring:
  amount-limit: 10000.00
  currency: INR
  max-transactions: 5
  window-minutes: 10
~~~

The monitoring code is deliberately easy to follow. Each rule is a normal
conditional block in **MonitoringService.java**.

## Main API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/transactions | Save a transaction and run all rules |
| GET | /api/transactions | Search and page through transactions |
| GET | /api/alerts | Filter and page through alerts |
| GET | /api/alerts/{id} | Read alert details, transactions, and history |
| PATCH | /api/alerts/{id}/status | Perform a valid status transition |
| GET | /api/rules | Read the effective monitoring rules |
| GET | /api/dashboard/summary | Read current IST-day totals |
| GET | /actuator/health | Check application health |

Ready-to-run examples are in [API examples](docs/api-examples.http).

## Database changes with Flyway

Flyway runs SQL files from **src/main/resources/db/migration** in version order.

The current migration is:

~~~text
V1__create_tables.sql
~~~

It creates the transaction, alert, link, and history tables. Flyway records the
completed version in **flyway_schema_history**, so it does not recreate the
tables on every start.

Never edit an applied production migration. Add the next version instead, for
example:

~~~text
V2__add_transaction_reference.sql
~~~

## Tests and JaCoCo

All end-to-end scenarios are kept in one readable test class:

~~~text
src/test/java/com/neueda/secureflow/SecureFlowTest.java
~~~

The tests use MockMvc like a browser and H2 like a temporary database. They test
the real controllers, services, repositories, Flyway migration, rules, errors,
dashboard, and static page without requiring a local MySQL installation.

JaCoCo watches which Java lines run during the tests. Maven fails during
**verify** when measured line coverage is lower than 70%.

See [Testing guide](docs/testing.md) for pass/failure rules and CI behavior.

## CI/CD

The workflow is **.github/workflows/pipeline.yml**.

For every pull request and push to main it:

1. installs Java 21
2. runs the Maven quality gate
3. uploads the JAR and JaCoCo report
4. builds the Docker image
5. publishes the image only after a successful push to main

Published image:

~~~text
ghcr.io/neueda-learning/114-secure-flow:latest
~~~

The workflow provides continuous delivery to GitHub Container Registry. It does
not automatically expose or restart a public server.

## Documentation

- [Architecture](docs/architecture.md)
- [Testing and coverage](docs/testing.md)
- [Docker deployment](docs/deployment.md)
- [API examples](docs/api-examples.http)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## Security boundary

SecureFlow currently has no login system or TLS termination. The supplied
Compose configuration is intentionally local-only. Do not expose port 8080 to
the public internet without authentication, HTTPS, secret management, backups,
and network controls.
