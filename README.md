# SecureFlow

SecureFlow is a beginner-friendly transaction-monitoring application built with Java 21, Spring Boot, MySQL, HTML and CSS.

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

`BUILD SUCCESS` means the code compiled, the tests passed, coverage passed, and the runnable JAR was created in `target`.

## Run the application

Create a MySQL database called `secureflow`, then set the credentials and start Spring Boot:

```powershell
$env:DB_USERNAME="secureflow"
$env:DB_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

Open:

- Dashboard: http://localhost:8080
- Health check: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html

## Important files

- `pom.xml` — dependencies, Java version, test and coverage setup.
- `application.yml` — database settings and monitoring thresholds.
- `V1__create_transactions_table.sql` — creates the transaction table.
- `TransactionEntity.java` — maps a Java transaction to a database row.
- `TransactionRepository.java` — provides database operations.
- `MonitoringProperties.java` — reads the three rule settings.
- `index.html` and `styles.css` — dashboard structure and appearance.
- `.github/workflows/ci.yml` — automatically tests every pull request and builds `main`.

## CI/CD in one sentence

GitHub Actions tests every pull request; after code reaches `main`, it tests again and publishes the runnable JAR as a downloadable artifact.

## Documentation

- Meeting Minutes: [docs/MoM/MoM-001-Business-Requirements.md](docs/MoM/MoM-001-Business-Requirements.md)
- User Stories: [docs/Requirements/User-Stories.md](docs/Requirements/User-Stories.md)
- Architecture Documentation: [docs/Architecture/System-Architecture.md](docs/Architecture/System-Architecture.md)
- Testing Strategy: [docs/Testing/Test-Strategy.md](docs/Testing/Test-Strategy.md)
- GitHub Kanban Board: Refer to the team's GitHub Project board used for delivery tracking.
