# Technology and dependency inventory

Learners who need code-level explanations of when these tools run and how they
pass or fail should use the
[supplementary technology learning guide](supplementary-technology-guide.md).

## Purpose, scope, and evidence

This inventory explains direct technologies used by the reviewed `main`
baseline. Versions come from `pom.xml`, the Maven Wrapper, Docker files,
workflow references, and a locally executed `mvnw dependency:tree` on
2026-08-06. Transitive dependencies are not exhaustively assessed here.

The resolved inventory is a strong starting point for an automated SBOM,
license, dependency-update, and vulnerability-scanning layer. Specialist review
remains the final step before distribution or production conclusions.

## Runtime and application technologies

| Technology | Verified version/reference | Purpose and location | Security, licensing, and operational considerations |
|---|---|---|---|
| Java | 21; `pom.xml`, Docker base images | Language/runtime for all backend code | Apply supported JDK updates; container and local JDK distributions can have different terms. [Java 21 documentation](https://docs.oracle.com/en/java/javase/21/) |
| Spring Boot | 4.0.7 parent | Application bootstrap, auto-configuration, dependency management | Large transitive surface; remain on a supported patch line and review actuator exposure. [4.0.7 reference](https://docs.spring.io/spring-boot/4.0/reference/index.html) |
| Spring MVC | 4.0.7 starter / Spring Framework 7.0.8 resolved | REST controllers, static assets, validation/error integration | The simple local endpoint model is easy to follow; add identity/authorization/CSRF controls before shared use. [Spring MVC reference](https://docs.spring.io/spring-framework/reference/web/webmvc.html) |
| Spring Data JPA / Hibernate | Spring Data JPA 4.0.6, Hibernate 7.2.19.Final resolved | Repository abstraction and entity mapping | Query/data-access changes require injection, authorization, and performance review. [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/) |
| Bean Validation | Hibernate Validator 9.0.1.Final resolved | Request-field validation | Validation is not authorization or business approval. Preserve server-side checks. [Hibernate Validator](https://hibernate.org/validator/documentation/) |
| Spring Actuator | 4.0.7 starter | `/actuator/health` and `/actuator/info` exposure | Keep exposure minimal; health data can reveal operational state. [Actuator endpoints](https://docs.spring.io/spring-boot/4.0/reference/actuator/endpoints.html) |
| MySQL | Docker image `mysql:8.4`; Connector/J 9.7.0 resolved | Persistent runtime database | Named-volume persistence is configured; immutable image identity, backups, encryption, hardening, retention, and licensing review are the next operational layers. [MySQL 8.4 manual](https://dev.mysql.com/doc/refman/8.4/en/) |
| Flyway | 11.14.1 resolved | Versioned schema migration at startup | V1 is the only migration. MySQL DDL rollback behavior requires care; never edit applied migrations. Review Community/commercial feature terms. [Flyway commands](https://documentation.red-gate.com/flyway/reference/commands) |
| springdoc-openapi | 3.0.3 | Runtime OpenAPI description and Swagger UI | Public “try it out” can call unauthenticated mutations. Disable/restrict in untrusted environments if appropriate. Community-maintained, not a Spring project. [springdoc documentation](https://springdoc.org/) |
| Plain HTML/CSS/JavaScript | Browser platform; no package manager | Dashboard and charts in `src/main/resources/static` | A small dependency surface keeps the UI approachable; browser compatibility/accessibility/security automation and continued escaping review form the next quality layer. [MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web) |

## Build and test technologies

| Technology | Verified version/reference | Purpose | Considerations |
|---|---|---|---|
| Maven Wrapper | Maven 3.9.16 resolved | Reproducible build entry point | The wrapper standardizes developer/CI builds; checksum verification can strengthen tool provenance. [Maven Wrapper](https://maven.apache.org/tools/wrapper/index.html) |
| Spring Boot Maven plugin | Managed by Boot 4.0.7 | Executable JAR packaging | A reproducible executable JAR is produced; signing and stronger provenance are release-maturity enhancements. |
| JUnit Jupiter | 6.0.3 resolved | Test execution | Seven integration-style methods cover primary flows; focused unit tests can complement them. [JUnit guide](https://docs.junit.org/current/user-guide/) |
| MockMvc / Spring Boot test | 4.0.7 | HTTP testing without a listening port | Fast HTTP-layer integration coverage is complemented by the Playwright/Compose system job. |
| H2 | 2.4.240 test scope | In-memory test database in MySQL mode | H2 provides fast repeatability; the MySQL 8.4 Compose system job supplies database-specific runtime evidence. [H2 documentation](https://h2database.com/html/main.html) |
| Playwright / axe-core | 1.62.1 / 4.12.1 dev scope | Chromium interaction and automated WCAG A/AA checks | Locked test-only dependencies; automated results do not replace manual accessibility review. [Playwright accessibility testing](https://playwright.dev/docs/accessibility-testing) |
| JaCoCo | 0.8.14 | Java bytecode line coverage report and 70% gate | Automated coverage is one quality signal; assertions and future expansion beyond current exclusions provide complementary confidence. [JaCoCo documentation](https://www.jacoco.org/jacoco/trunk/doc/) |
| k6 | Unpinned `grafana/k6` container tag | Manual gradual and 1,000-VU spike scripts | Intentional manual execution protects normal workflows; pin the image, retain evidence, and add a correctness threshold for repeatable benchmarking. [k6 API load testing](https://grafana.com/docs/k6/latest/testing-guides/api-load-testing/) |

## Delivery and infrastructure

| Technology | Reference | Purpose | Considerations |
|---|---|---|---|
| Docker | Multi-stage `Dockerfile` | Build JAR and run it as a non-root application user | Multi-stage/non-root packaging is implemented; digests, scanning, SBOM, and signing are the next supply-chain controls. [Dockerfile reference](https://docs.docker.com/reference/dockerfile/) |
| Docker Compose | `compose.yaml` | Start application plus MySQL and retain a named volume | Loopback binding, health checks, ordering, and persistence suit local use; managed credentials, backup, TLS, resources, and log rotation extend it for shared use. [Docker Compose](https://docs.docker.com/compose/) |
| GitHub Actions | `.github/workflows/pipeline.yml` | Maven quality, MySQL/Compose/browser system checks, artifacts, and GHCR publication | Main run 31098653366 passed all jobs; pinning action SHAs and adding security/provenance gates strengthen the delivery layer. [GitHub Actions reference](https://docs.github.com/en/actions/reference) |
| GitHub Container Registry | `ghcr.io/neueda-learning/114-secure-flow:latest` | Published application image | Publication succeeded for `main` commit `13738e3`; immutable SHA/digest tags, signing and pull/deployment verification complete release provenance. [Container registry docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry) |
| Git/GitHub | Repository history, issues, PRs | Collaboration and evidence | `main` protection verifies required PRs, one approval, and `test-and-package`; actual approvals remain PR-specific evidence. Repository visibility reinforces the no-sensitive-data rule. |
| GitHub Projects | Private organization Project #18 | Kanban planning, ownership, status flow, priority, burn-up/status insights, and issue/PR traceability | The authenticated 2026-08-06 snapshot is recorded as `EVD-AGILE-001`; access and retention depend on organization settings and GitHub terms. Public issue/PR links preserve a portable evidence layer, and board status is not a substitute for PR/test evidence. [GitHub Projects documentation](https://docs.github.com/en/issues/planning-and-tracking-with-projects/learning-about-projects/about-projects) |

## External services and integrations

The application has a deliberately small runtime integration surface: MySQL and
the local browser. Build/delivery uses Maven Central, container registries, and
GitHub services, making network, package-integrity, and registry-permission
controls straightforward to identify and manage.

## Replacement and migration considerations

- Replacing MySQL requires verifying SQL, migration, JDBC, and time semantics.
- Replacing H2 with Testcontainers/MySQL would improve parity at increased
  Docker/time complexity.
- Splitting the static frontend requires a new build/runtime/dependency model.
- Replacing GitHub Actions/GHCR requires equivalent quality gates, evidence,
  artifact retention, permissions, and secret controls.
- Upgrading major Spring Boot or Java versions requires compatibility,
  migration, regression, and container validation.

## Licensing readiness plan

1. Project owner must choose and add a repository-level license or document
   that redistribution is not permitted. No `LICENSE` file exists.
2. Generate and review a full transitive dependency and container license
   report before distribution.
3. Confirm base-image, MySQL, Flyway, Swagger UI, k6, and JDK distribution terms.
4. Preserve required notices and source obligations.

These are governance actions, not legal conclusions.

## Maintenance

Regenerate the dependency tree after upgrades and update versions, risks, and
links. Do not infer the current version of a managed dependency from memory;
record the resolved build output.
