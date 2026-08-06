# Supplementary technology learning guide

## Purpose

This guide explains the SecureFlow technologies that go beyond writing basic
Java, Spring controllers, SQL, HTML, CSS, and JavaScript. It is written for a
beginner who needs to understand the code, demonstrate the project, and answer
simple questions about when each tool runs and how it succeeds or fails.

## Scope and course-material status

The project owner identified Flyway, MockMvc, JaCoCo, and k6 as technologies
that were not taught in the class material. That classification is recorded as
owner-provided context, not as an independent review of the complete syllabus.

Source inspection found other project-specific technologies that are important
to understand. Their classroom coverage has not been independently verified,
so they are labelled **confirm with the instructor** rather than “not taught.”

| Group | Technologies | Course-material status |
|---|---|---|
| Confirmed supplementary topics | Flyway, MockMvc, JaCoCo, k6 | Identified by the project owner as not taught in class |
| Build and packaging | Maven lifecycle, Maven Wrapper, executable JAR | Confirm with instructor |
| Persistence and validation | H2, Spring Data JPA, Hibernate schema validation, Jakarta Bean Validation | Confirm with instructor |
| API and operations | Actuator health, springdoc-openapi, Swagger UI | Confirm with instructor |
| Delivery | Docker multi-stage build, Docker Compose health ordering, GitHub Actions, GHCR | Confirm with instructor |
| Browser quality | Playwright and axe-core | Confirm with instructor |

## The complete project flow

```text
Developer runs Maven or pushes code
              |
              v
        application starts
              |
              +-> Flyway builds/checks the database schema
              +-> Hibernate validates Java mappings against that schema
              |
              v
       MockMvc runs HTTP tests
              |
              +-> H2 acts as the fast test database
              +-> JaCoCo records executed Java lines
              |
              v
       Maven builds the executable JAR
              |
              v
 Docker builds an image -> Compose starts MySQL + app
              |
              +-> Actuator reports health
              +-> Playwright uses the real browser UI
              +-> axe checks common accessibility rules
              |
              v
 GitHub Actions publishes the image to GHCR after required jobs pass

k6 is separate and manual: it sends many requests only when a person starts it.
```

## 1. Flyway: database version control

### Beginner meaning

Flyway is like a numbered instruction book for the database. Instead of a
developer manually creating tables, the application finds numbered SQL files
and applies missing versions in order.

### Where SecureFlow uses it

- Dependency: [`pom.xml`](../pom.xml)
- Migration folder: [`src/main/resources/db/migration/`](../src/main/resources/db/migration/)
- First migration: [`V1__create_tables.sql`](../src/main/resources/db/migration/V1__create_tables.sql)
- Runtime configuration: [`application.yml`](../src/main/resources/application.yml)
- Test configuration: [`src/test/resources/application.yml`](../src/test/resources/application.yml)

The filename has meaning:

```text
V1__create_tables.sql
^^  ^
|   description
version 1
```

The real migration begins by creating the application tables:

```sql
CREATE TABLE transactions (...);
CREATE TABLE alerts (...);
CREATE TABLE alert_transactions (...);
CREATE TABLE alert_status_history (...);
```

### When it runs

Flyway runs automatically while Spring Boot is starting:

1. Spring connects to the configured database.
2. Flyway checks its `flyway_schema_history` table.
3. If V1 has not run, Flyway executes `V1__create_tables.sql`.
4. Flyway records V1 and its checksum in the history table.
5. Later starts see that V1 already ran and do not create the tables again.
6. Hibernate then validates that the entity mappings match the schema.

This happens with MySQL in the running application and with H2 during the Maven
tests.

### Success and failure

- **Success:** every pending migration runs and its result is recorded.
- **Failure:** invalid SQL, a database connection problem, or an unexpected
  change to an already-recorded migration stops application startup.
- **Important rule:** after V1 has been shared, create `V2__description.sql`
  for the next change instead of silently editing database history.

Official reference: [Flyway migrations](https://documentation.red-gate.com/fd/migrations-271585107.html).

## 2. MockMvc: testing the HTTP API without opening a port

### Beginner meaning

MockMvc behaves like a small test client inside the Spring application. It sends
fake HTTP requests through the real Spring MVC controller pipeline, but it does
not start Chrome and does not need `localhost:8080`.

### Where SecureFlow uses it

The complete test class is
[`SecureFlowTest.java`](../src/test/java/com/neueda/secureflow/SecureFlowTest.java):

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecureFlowTest {
    @Autowired
    MockMvc mockMvc;
}
```

A real project example is:

```java
mockMvc.perform(post("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .content(transaction(" ACC-1 ", " PAYEE-1 ", "250.50")))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.transaction.currency").value("INR"));
```

Read it from left to right:

1. create a `POST /api/transactions` request
2. say the body is JSON
3. send test transaction data
4. expect HTTP `201 Created`
5. expect the response currency to equal `INR`

### When it runs

MockMvc tests run during Maven's `test` phase, which is included in:

```powershell
.\mvnw.cmd clean verify
```

The same command runs in the GitHub Actions `test-and-package` job.

### Success and failure

- **Success:** every `.andExpect(...)` assertion is true and the method throws
  no unexpected exception.
- **Failure:** even one wrong status, missing JSON field, wrong value, or
  exception fails that test and Maven exits non-zero.
- `@BeforeEach` deletes earlier alerts and transactions so each test begins
  with a known database state.

Official reference: [Spring MockMvc](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html).

## 3. JaCoCo: measuring which Java lines ran

### Beginner meaning

JaCoCo does not decide whether the business behavior is correct. The test
assertions do that. JaCoCo records which measured Java lines were executed while
the tests ran and calculates a coverage percentage.

### Where SecureFlow configures it

[`pom.xml`](../pom.xml) declares JaCoCo `0.8.14` and three goals:

```xml
<goal>prepare-agent</goal>
<goal>report</goal>
<goal>check</goal>
```

The project rule is:

```xml
<counter>LINE</counter>
<value>COVEREDRATIO</value>
<minimum>0.70</minimum>
```

This means at least 70% of the included Java lines must run. Application entry
points, entity boilerplate, DTOs, and configuration are excluded by the current
rule, so the number applies to the configured measurement scope—not every line
in the repository.

### When it runs

```text
Maven starts tests
  -> JaCoCo agent records executed bytecode
  -> tests finish
  -> verify phase creates the report
  -> check goal compares coverage with 70%
```

Open the local report after `clean verify`:

```text
target/site/jacoco/index.html
```

GitHub Actions also uploads this report with the JAR as `secureflow-build`.

### Success and failure

- **Success:** all tests pass and configured line coverage is at least 70%.
- **Failure:** a test failure stops the build, or the JaCoCo check fails when
  coverage is below 70%.
- High coverage alone is not proof of correct assertions, security, or complete
  edge-case testing.

Official reference: [JaCoCo documentation](https://www.jacoco.org/jacoco/trunk/doc/).

## 4. k6: manually applying API load

### Beginner meaning

k6 is a load-testing program. It runs JavaScript scenarios that send many HTTP
requests and measure error rate and response time. It is not JUnit, MockMvc, or
a normal browser test.

### Where SecureFlow uses it

- Gradual scenario: [`gradual-ramp.js`](../load-tests/gradual-ramp.js)
- 1,000-user scenario: [`spike-1000-concurrent.js`](../load-tests/spike-1000-concurrent.js)
- Docker launcher: [`run.sh`](../load-tests/run.sh)
- Usage/evidence rules: [`load-tests/README.md`](../load-tests/README.md)

The spike configuration is:

```javascript
instant_1000: {
  executor: "shared-iterations",
  vus: 1000,
  iterations: 1000,
  maxDuration: "2m",
}
```

It asks k6 to use up to 1,000 virtual users to complete 1,000 total iterations.
Each iteration sends one transaction request.

Its pass/fail thresholds are:

```javascript
http_req_failed: ["rate<0.15"],
http_req_duration: ["p(95)<4000", "p(99)<8000"],
```

In beginner language:

- fewer than 15% of HTTP requests may fail
- 95% should finish in under 4 seconds
- 99% should finish in under 8 seconds

The `check` also records whether each response returned HTTP 201. The current
thresholds govern k6's process exit; the guide recommends adding a threshold
for the check rate before treating it as a strict correctness gate.

### How Docker supplies k6

The project does not permanently install k6. [`run.sh`](../load-tests/run.sh)
runs the `grafana/k6` container, mounts the scripts read-only for execution, and
removes the container when finished:

```bash
docker run --rm -i ... -v "$script_dir:/scripts" \
  -e BASE_URL="$base_url" \
  grafana/k6 run "/scripts/$target"
```

Run it only against an authorized disposable environment:

```bash
cd load-tests
BASE_URL=http://127.0.0.1:8080 ./run.sh gradual
BASE_URL=http://127.0.0.1:8080 ./run.sh spike
```

### When it runs

k6 is intentionally manual. It is not part of `mvn verify` or normal CI because
1,000 concurrent virtual users can consume significant CPU, memory, database
connections, and data.

### Success and failure

- **Success:** k6 finishes and every configured threshold passes, producing
  exit code 0.
- **Failure:** a threshold fails, k6 cannot reach the application, or the
  scenario exceeds its allowed duration.
- A result is meaningful only with the commit, environment, command, and raw
  output recorded together.

Official reference: [k6 thresholds](https://grafana.com/docs/k6/latest/using-k6/thresholds/).

## 5. Maven lifecycle, Wrapper, and executable JAR

### Maven and the Wrapper

Maven reads [`pom.xml`](../pom.xml), downloads dependencies, compiles Java,
runs tests, invokes JaCoCo, and packages the application.

The Wrapper files (`mvnw`, `mvnw.cmd`, and `.mvn/wrapper`) make the project use
the configured Maven `3.9.16` download instead of relying on whichever Maven a
developer happens to have installed.

```text
clean -> removes the old target directory
compile -> compiles main code
test -> runs the seven tests
package -> builds the JAR
verify -> runs later quality checks, including JaCoCo
```

Only the last requested phase needs to be typed because Maven runs the earlier
phases in order:

```powershell
.\mvnw.cmd clean verify
```

Official reference: [Maven build lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

### What the JAR is

JAR means **Java Archive**. It is a ZIP-like package containing compiled classes,
resources, dependencies/loader information, and a manifest. Spring Boot's Maven
plugin makes this project JAR executable:

```bash
java -jar target/secureflow-1.0.0.jar
```

The application succeeds when Spring finishes startup and Actuator reports
`UP`. It fails early if configuration, Flyway, Hibernate validation, the
database connection, or another required startup component fails.

Official reference: [Spring Boot executable archives](https://docs.spring.io/spring-boot/maven-plugin/packaging.html).

## 6. H2, Spring Data JPA, and Hibernate validation

### H2 test database

H2 is a lightweight database used only by the Maven tests. The test URL in
[`src/test/resources/application.yml`](../src/test/resources/application.yml)
is:

```yaml
url: jdbc:h2:mem:secureflow;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
```

- `mem` means the database lives in memory.
- `MODE=MySQL` makes some SQL behavior closer to MySQL.
- it starts quickly and disappears after the test process.
- it is useful fast feedback, but it is not perfect MySQL equivalence; the CI
  Compose job separately starts real MySQL 8.4.

Official reference: [H2 documentation](https://h2database.com/html/main.html).

### Spring Data JPA

JPA maps Java entities to database rows. Spring Data creates implementations of
repository interfaces such as
[`TransactionRepository`](../src/main/java/com/neueda/secureflow/transaction/TransactionRepository.java):

```java
public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long> {
    boolean existsByAccountIdAndPayeeId(String accountId, String payeeId);
}
```

The team writes the interface and query intent; Spring supplies standard
save/find/delete behavior at runtime.

Official reference: [Spring Data JPA getting started](https://docs.spring.io/spring-data/jpa/reference/jpa/getting-started.html).

### Hibernate `ddl-auto: validate`

Both runtime and test configuration use:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```

Flyway owns schema changes. Hibernate only checks whether entity mappings fit
the schema. If a required table or column does not match, application startup
fails instead of silently changing the database.

## 7. Jakarta Bean Validation

Validation annotations place simple input rules next to request fields. The
transaction request in
[`CreateTransactionRequest.java`](../src/main/java/com/neueda/secureflow/transaction/dto/CreateTransactionRequest.java)
includes:

```java
@NotBlank
@Size(min = 3, max = 50)
@Pattern(regexp = "\\s*[A-Za-z0-9-]+\\s*")
String accountId,

@NotNull @DecimalMin("0.01") BigDecimal amount
```

The controller activates these annotations with `@Valid`. Invalid input is
rejected before the normal service logic runs. MockMvc verifies representative
HTTP 400 responses in `badRequestsHaveClearErrors()`.

Official reference: [Jakarta Validation 3.1](https://jakarta.ee/specifications/bean-validation/3.1/).

## 8. Actuator health checks

Spring Boot Actuator provides small operational endpoints. SecureFlow exposes
only `health` and `info`:

```yaml
management.endpoints.web.exposure.include: health,info
```

Docker calls `/actuator/health` to decide whether the application container is
healthy. Compose waits for the database health check before starting the app,
and GitHub Actions waits for both services before running system tests.

- **Success:** the health endpoint returns HTTP 200 with status `UP`.
- **Failure:** Docker's configured retries are exhausted and the container is
  marked unhealthy.
- Shared environments should protect operational endpoints and avoid exposing
  unnecessary details.

Official reference: [Spring Boot Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html).

## 9. springdoc-openapi and Swagger UI

`springdoc-openapi` reads Spring controllers and generates an OpenAPI
description of the REST API. Swagger UI turns that description into an
interactive browser page.

[`pom.xml`](../pom.xml) includes `springdoc-openapi-starter-webmvc-ui` version
`3.0.3`, and [`application.yml`](../src/main/resources/application.yml) sets:

```yaml
springdoc.swagger-ui.path: /swagger-ui.html
```

When the application is running:

- `/v3/api-docs` returns the generated JSON description
- `/swagger-ui.html` opens the interactive API page

Official reference: [springdoc-openapi](https://springdoc.org/).

## 10. Docker multi-stage build and Compose ordering

### Multi-stage Dockerfile

[`Dockerfile`](../Dockerfile) has two `FROM` stages:

```dockerfile
FROM maven:3.9.11-eclipse-temurin-21 AS build
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /project/target/secureflow-*.jar app.jar
USER secureflow
ENTRYPOINT ["java", "-jar", "app.jar"]
```

The first image has Maven and builds the JAR. The final image receives only the
JAR and Java runtime, then runs as the non-root `secureflow` user. This keeps
build tools out of the runtime image.

Official reference: [Docker multi-stage builds](https://docs.docker.com/build/building/multi-stage/).

### Compose

[`compose.yaml`](../compose.yaml) describes two services and their connection:

```text
MySQL starts -> MySQL health succeeds -> app starts -> app health succeeds
```

The `mysql-data` named volume keeps database files when containers are stopped
and restarted. `docker compose down --volumes` deliberately removes that data.

Official reference: [Docker Compose](https://docs.docker.com/compose/).

## 11. GitHub Actions and GHCR

The workflow in [`.github/workflows/pipeline.yml`](../.github/workflows/pipeline.yml)
runs after pull requests and pushes to `main`.

```text
test-and-package
  -> Maven tests + Flyway + JaCoCo + JAR

container-system-test
  -> MySQL + Compose + persistence + Playwright + axe

deliver-image (push to main only)
  -> waits for both jobs -> publishes Docker image to GHCR
```

A job fails when a command returns a non-zero exit code. `deliver-image` has
`needs` dependencies, so it does not run successfully unless both required jobs
succeed. GHCR means GitHub Container Registry, the service that stores the
published Docker image.

Official references: [GitHub Actions](https://docs.github.com/en/actions) and
[GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).

## 12. Playwright and axe-core

### Playwright

MockMvc tests the Spring HTTP pipeline. Playwright goes further: it launches
Chromium and behaves like a user. The tests in
[`dashboard.spec.js`](../browser-tests/dashboard.spec.js) open the page, fill
the transaction form, click Submit, wait for the success message, inspect the
table, and change the chart tab.

[`playwright.config.js`](../playwright.config.js) retains traces and screenshots
on failure. A Playwright assertion timeout, unexpected browser state, or JavaScript
error affecting an expected result fails the test command.

Official reference: [Playwright Test](https://playwright.dev/docs/intro).

### axe-core

axe-core examines the rendered page for rules that can be checked
automatically:

```javascript
const results = await new AxeBuilder({page})
    .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"])
    .analyze();

expect(results.violations).toEqual([]);
```

The test fails if the returned `violations` array is not empty. Automated axe
coverage is valuable but does not replace keyboard, screen-reader, zoom,
reflow, cognitive, and human accessibility review.

Official reference: [axe-core project](https://github.com/dequelabs/axe-core).

## How the main commands differ

| Command | Main purpose | Starts real MySQL? | Starts a browser? | Sends load? |
|---|---|---:|---:|---:|
| `.\mvnw.cmd clean verify` | Java tests, Flyway/H2, JAR, JaCoCo | No | No | No |
| `docker compose up --build --wait` | Run complete application | Yes | No | No |
| `npm run test:browser` | Playwright and axe checks | Uses already-running app | Yes | No |
| `./load-tests/run.sh gradual` | Gradual k6 load | Uses already-running app | No | Yes |
| `./load-tests/run.sh spike` | 1,000-VU k6 spike | Uses already-running app | No | Yes |

## Simple presentation explanations

- **Flyway:** “It runs our numbered SQL migration automatically and records
  which database versions have already been applied.”
- **MockMvc:** “It sends test HTTP requests through Spring without opening a
  real network port.”
- **JaCoCo:** “It measures which Java lines our tests executed and fails the
  Maven build below our configured 70% scope.”
- **k6:** “It is a separate manual load tool that sends many API requests and
  compares error/response-time metrics with thresholds.”
- **H2:** “It is the fast in-memory database for Maven tests; CI also checks the
  application against real MySQL.”
- **JAR:** “It is the packaged runnable Java application.”
- **Actuator:** “Docker asks its health endpoint whether the app is ready.”
- **Playwright:** “It tests the real page in Chromium.”
- **axe-core:** “It automatically checks a useful subset of accessibility
  rules, with manual accessibility testing still complementary.”

## Evidence and limitations

- Versions and implementation references come from the reviewed source
  baseline and [technology inventory](technology-inventory.md).
- Verified commands/results are indexed in [testing](testing.md) and the
  [evidence index](evidence-index.md).
- The four confirmed course-status classifications come from the project owner.
- Classification of other technologies requires the authoritative syllabus or
  instructor confirmation.
- This guide explains the current implementation; it does not replace official
  product documentation or claim mastery by an individual contributor.

## Related documents

- [Technology and dependency inventory](technology-inventory.md)
- [Architecture](architecture.md)
- [Testing](testing.md)
- [Deployment](deployment.md)
- [Development guide](development.md)
- [Mentor rubric evidence guide](mentor-review-guide.md)
- [Evidence index](evidence-index.md)
