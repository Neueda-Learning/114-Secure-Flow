# SecureFlow

SecureFlow is a beginner-friendly transaction-monitoring application built with Java 21, Spring Boot, MySQL, HTML, CSS and JavaScript.

It saves transactions, checks monitoring rules immediately, creates alerts, and lets an operator move alerts through a simple investigation workflow.

## Simple team workflow

1. Start from `main`: `git checkout main` then `git pull origin main`.
2. Create one feature branch: `git checkout -b feature/SF-XX-short-name`.
3. Make one small change and add a test.
4. Run `mvnw.cmd clean verify` on Windows.
5. Commit, push, and open a pull request into `main`.
6. Another teammate reviews it. Merge only when CI is green.

Rudra reviews teammate pull requests. Rushil reviews Rudra's pull requests. Nobody merges their own pull request.

## Run the tests

```powershell
.\mvnw.cmd clean verify
```

`BUILD SUCCESS` means the code compiled, tests passed, coverage passed, and the runnable JAR was created in `target`.

## Run the application

Create a MySQL database named `secureflow`, then run:

```powershell
$env:DB_USERNAME="secureflow"
$env:DB_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

Open:

- Dashboard: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html

## What works

- Create and search transactions.
- New-payee alert on the first account/payee payment.
- Velocity alert on the sixth transaction in ten minutes.
- Amount rule: payments strictly greater than ₹10,000.00 INR create a high-severity alert.
- Alert flow: `OPEN -> ACKNOWLEDGED -> INVESTIGATING -> CLOSED`.
- Close/dismiss requires resolution notes and every move is saved in history.
- Summary cards, rule cards, responsive tables and friendly error states.
- Flyway MySQL schema, Swagger, health check, tests, 70% coverage, JAR and Docker delivery.

## Important files in simple words

- `pom.xml` — libraries, Java version, tests and coverage.
- `application.yml` — database connection and the three rule settings.
- `V1__create_transactions_table.sql` — creates the transaction table.
- `V2__create_alert_tables.sql` — creates alert and history tables.
- `TransactionController.java` — receives transaction API requests.
- `TransactionService.java` — saves a transaction and starts monitoring.
- `MonitoringRule.java` — the small contract each monitoring rule follows.
- `NewPayeeRule.java` and `VelocityRule.java` — the two monitoring checks.
- `AlertService.java` — creates alerts and controls legal status changes.
- `AlertController.java` — exposes alert endpoints to the dashboard.
- `index.html` — dashboard structure.
- `styles.css` — colours, spacing and mobile layout.
- `transaction-form.js` — validates and submits the form.
- `app.js` — loads tables, cards and alert actions from the API.
- `ci.yml` — tests pull requests and smoke-tests MySQL.
- `release.yml` — publishes the Docker image from `main`.

## How one transaction flows

1. `TransactionController` receives JSON from the form.
2. `TransactionService` saves a `TransactionEntity`.
3. `MonitoringService` gives it to every `MonitoringRule`.
4. A matching rule returns a `RuleMatch`.
5. `AlertService` saves an alert and its first history entry.
6. `app.js` reloads the data and updates the dashboard.

See [architecture](docs/architecture.md) and the [demo script](docs/demo-script.md).

## CI/CD in one sentence

GitHub Actions tests every pull request; after code reaches `main`, it tests again, smoke-tests MySQL, publishes the JAR artifact, and publishes a Docker image.

## Documentation

- Meeting Minutes: [docs/MoM/MoM-001-Business-Requirements.md](docs/MoM/MoM-001-Business-Requirements.md)
- User Stories: [docs/Requirements/User-Stories.md](docs/Requirements/User-Stories.md)
- Architecture Documentation: [docs/Architecture/System-Architecture.md](docs/Architecture/System-Architecture.md)
- Testing Strategy: [docs/Testing/Test-Strategy.md](docs/Testing/Test-Strategy.md)
- GitHub Kanban Board: refer to the team's GitHub Project board used for delivery tracking.
