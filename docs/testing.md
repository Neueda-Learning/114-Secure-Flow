# Testing and coverage

## When tests run

Tests run:

- locally when a developer executes **mvnw clean verify**
- in GitHub Actions for every pull request
- in GitHub Actions for every push to main

Tests do not run when a normal JAR starts. Flyway does run during normal startup.

## Complete local command

Windows:

~~~powershell
.\mvnw.cmd clean verify
~~~

Linux or macOS:

~~~bash
chmod +x mvnw
./mvnw clean verify
~~~

## Maven stages

~~~text
clean → compile → test → package → verify
~~~

- **clean** removes the previous target directory.
- **compile** checks and compiles production Java.
- **test** starts JUnit and runs every method marked @Test.
- **package** creates the runnable Spring Boot JAR.
- **verify** creates the JaCoCo report and enforces the coverage limit.

A compile error, failed assertion, unhandled exception, or coverage violation
makes Maven return a non-zero exit code and print **BUILD FAILURE**.

## Test structure

The test suite is intentionally in one file:

~~~text
src/test/java/com/neueda/secureflow/SecureFlowTest.java
~~~

**@SpringBootTest** starts the real Spring components.

**@AutoConfigureMockMvc** provides a test HTTP client without opening a public
network port.

Before every scenario:

~~~java
@BeforeEach
void emptyDatabase() {
    alerts.deleteAll();
    transactions.deleteAll();
}
~~~

This prevents one test's data from changing another test's result.

## Test database and Flyway

Tests use an in-memory H2 database configured in
**src/test/resources/application.yml**.

Flyway applies **V1__create_tables.sql** when the test application starts.
Hibernate then validates that the Java entity mappings match those tables.

This verifies the migration without requiring a developer to install MySQL.

## Covered scenarios

The six end-to-end tests cover:

1. transaction creation, normalization, and search filters
2. amount and velocity monitoring
3. complete alert lifecycle and invalid transitions
4. dismissal and required resolution notes
5. validation, unsupported currency, bad ranges, and missing resources
6. dashboard summary, rule endpoint, alert filters, and static page

A test succeeds only when every expectation matches the actual HTTP response.

Example:

~~~java
.andExpect(status().isCreated())
.andExpect(jsonPath("$.generatedAlerts[0].ruleType")
        .value("AMOUNT_THRESHOLD"));
~~~

The test fails if the status is not 201 or the alert type is different.

## JaCoCo

JaCoCo records which Java lines execute while tests run. It does not decide
whether the returned answer is correct; JUnit assertions do that.

The Maven gate requires:

~~~text
line coverage >= 70%
~~~

The HTML report is written to:

~~~text
target/site/jacoco/index.html
~~~

Configuration records, DTOs, entities, and the main method are excluded from the
minimum gate so the percentage focuses on controller and business behavior.

## GitHub Actions result

The pipeline runs:

~~~bash
./mvnw --batch-mode clean verify
~~~

Exit code 0 gives the workflow a green result. Any other exit code stops the
pipeline before delivery.

After Maven passes, GitHub also builds the Dockerfile. The image is published
only for a successful push to main.
