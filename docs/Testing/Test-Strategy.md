# Test Strategy

## Testing Objectives

- Verify that transaction monitoring behavior meets business requirements.
- Prevent regressions in API behavior, validation rules, and dashboard shell behavior.
- Ensure changes remain buildable, testable, and packageable in CI.
- Maintain agreed minimum automated coverage requirements.

## Unit Testing Approach

- Use focused unit-style tests for isolated logic and data structures.
- Validate entity and configuration behavior with deterministic inputs.
- Keep unit tests fast and independent from external services.
- Store unit tests under src/test/java following package structure.

## Integration Testing Approach

- Use Spring Boot test slices/full context tests for controller and service integration paths.
- Execute tests with the repository's test profile and in-memory H2 database configuration.
- Validate that request handling, validation, persistence behavior, and response mapping work together.

## API Testing Approach

- Use MockMvc-based tests for HTTP endpoint verification.
- Verify success paths (2xx), validation failures (4xx), and response payload shape.
- Include representative request/response scenarios for transaction endpoints.

## Validation Testing

- Validate required fields, numeric thresholds, and format constraints.
- Confirm invalid inputs are rejected with correct HTTP status behavior.
- Confirm normalized values (for example currency formatting behavior) are reflected in API responses.

## Test Automation Process

- Local execution command:

```powershell
.\mvnw.cmd clean verify
```

- The command performs compile, test execution, JaCoCo reporting, and coverage check.
- BUILD SUCCESS is the required local baseline before opening a pull request.

## CI/CD Testing Flow

- GitHub Actions workflow triggers on:
  - pull requests targeting main
  - pushes to main
- CI steps:
1. Checkout repository.
2. Set up Java 21.
3. Run Maven wrapper with clean verify.
4. Upload runnable JAR artifact.
5. Upload JaCoCo report artifact.

## Coverage Requirements

- Coverage is enforced by jacoco-maven-plugin during verify.
- Current rule requires minimum line coverage ratio of 0.70 at bundle level.
- Application bootstrap and entity classes are excluded per existing Maven configuration.

## Pre-PR Verification Checklist for Developers

1. Pull latest main and rebase/sync your feature branch.
2. Run local command: .\mvnw.cmd clean verify.
3. Confirm all tests pass and coverage check passes.
4. Review changed files to ensure test updates accompany behavior changes.
5. Push branch and open pull request only after local verification is green.
