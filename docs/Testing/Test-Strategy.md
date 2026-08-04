# Test Strategy

## Objectives

- Protect monitoring-rule boundaries and alert lifecycle behavior.
- Verify request validation, response status, filtering, and pagination.
- Exercise persistence mappings and Flyway migrations.
- Keep the dashboard's required controls and assets present.
- Prove that the packaged application starts against MySQL.
- Keep the Linux container deployment reproducible.

## Automated test layers

| Layer | Focus | Main tools |
|---|---|---|
| Unit | Rules, entities, configuration, status transitions | JUnit 5, Mockito |
| Web/API | Controllers, validation, status codes, JSON payloads | MockMvc |
| Integration | Spring context, repositories, monitoring flow, migrations | Spring Boot Test, H2 |
| Packaging smoke test | Runnable JAR, MySQL connection, Flyway, health | GitHub Actions, MySQL 8.4, curl |
| Deployment validation | Compose model and multi-stage image build | Docker Compose, Docker BuildKit |

## Local quality gate

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```bash
./mvnw clean verify
```

`verify` compiles all code, runs the test suite, produces the JaCoCo report,
enforces coverage, and packages the executable JAR. A successful local build is
required before opening a pull request.

## Coverage

The Maven build enforces at least 70% bundle-level line coverage for non-trivial
backend code. Bootstrap, configuration, DTO, and entity boilerplate are excluded
from the threshold. The HTML report is generated at
`target/site/jacoco/index.html`.

Coverage is a regression signal, not a substitute for acceptance testing. Tests
must assert meaningful boundaries such as ₹10,000 versus ₹10,000.01, the fifth
versus sixth transaction, valid and invalid transitions, and malformed filters.

## CI flow

1. Check out the exact commit.
2. Install Java 21 and cache Maven dependencies.
3. Run `clean verify`.
4. Start the packaged JAR against a MySQL 8.4 service.
5. Wait for `/actuator/health` to report healthy.
6. Validate `compose.yaml` and build the runtime image.
7. Upload the runnable JAR and JaCoCo report.

On `main` and version tags, continuous delivery repeats verification and
publishes commit-addressable and release-tagged images to GitHub Container
Registry.

## Manual acceptance

Before the presentation, follow [presentation-checklist.md](../presentation-checklist.md)
on the actual Linux VM and exercise all three rule scenarios and the complete
alert workflow in a browser.
