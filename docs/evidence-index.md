# Evidence index

## Purpose and rules

This catalogue links significant claims to reproducible or persistent evidence.
Each entry records its support, verified boundary, and next evidence step. The
review preserves authentic screenshots, logs, benchmarks, approvals,
deployments, and test results without manufacturing substitutes.

## Evidence catalogue

### EVD-SRC-001: Reviewed source baseline

| Field | Value |
|---|---|
| Description | Git tree on `main` commit `9379af1` |
| Supports | Current implemented files, architecture, configuration, tests |
| Source | [GitHub commit](https://github.com/Neueda-Learning/114-Secure-Flow/commit/9379af19e7194a4a5b8e0a22eb4f34e141b1a503) |
| Verification | Clean worktree created from `origin/main`; source/configuration inspection |
| Date | 2026-08-06 |
| Validity | Valid for this commit only |
| Boundary / next evidence | Add runtime, security, and deployment verification when those scopes are exercised |

### EVD-TEST-001: Current integration-test source

| Field | Value |
|---|---|
| Description | Seven Spring Boot/MockMvc/H2 test methods |
| Supports | Test intent, assertions, setup, and covered scenarios |
| Source | [`SecureFlowTest.java`](../src/test/java/com/neueda/secureflow/SecureFlowTest.java) |
| Verification | Source inspection and CI execution in EVD-CI-001 |
| Date | Current at baseline |
| Validity | Changes with test source |
| Boundary / next evidence | Passing CI is linked separately; EVD-SYSTEM-001 adds real-MySQL system parity |

### EVD-CI-001: Main quality gate and registry follow-up

| Field | Value |
|---|---|
| Description | GitHub Actions run for `main` commit `9379af1` |
| Supports | 7 tests passed; 1 Flyway migration validated/applied in H2 context; JaCoCo gate/Maven/JAR artifact and Docker image construction passed; GHCR step returned `denied` |
| Source | [Workflow run 31084398909](https://github.com/Neueda-Learning/114-Secure-Flow/actions/runs/31084398909) |
| Verification | Job/step/log inspection through GitHub API |
| Date | 2026-08-06 |
| Validity | Persistent for the recorded commit/run |
| Boundary / next evidence | Overall status is failed at registry publication; align permissions, retain the image digest and exact coverage report, then verify deployment separately |

### EVD-CI-002: Draft Linux CD PR quality result

| Field | Value |
|---|---|
| Description | PR #46 CI run |
| Supports | Proposed branch passed Maven/Docker build; deployment job skipped on PR as configured |
| Source | [PR #46](https://github.com/Neueda-Learning/114-Secure-Flow/pull/46), [run 31090156968](https://github.com/Neueda-Learning/114-Secure-Flow/actions/runs/31090156968) |
| Verification | GitHub job/step inspection |
| Date | 2026-08-06 |
| Validity | Branch commit `4fc2c00` only |
| Boundary / next evidence | Draft branch evidence only; add supervised server/environment deployment evidence before merge |

### EVD-CI-003: Open chart PR quality result

| Field | Value |
|---|---|
| Description | PR #47 CI run |
| Supports | Proposed chart branch passed configured CI |
| Source | [PR #47](https://github.com/Neueda-Learning/114-Secure-Flow/pull/47), [run 31091445945](https://github.com/Neueda-Learning/114-Secure-Flow/actions/runs/31091445945) |
| Verification | GitHub commit/run lookup |
| Date | 2026-08-06 |
| Validity | Branch commit `e2d4a7f` only |
| Boundary / next evidence | Unmerged contribution; retain visual/environment artifacts if the manual claims are used for approval |

### EVD-DB-001: Versioned schema

| Field | Value |
|---|---|
| Description | Flyway V1 creates four application tables and indexes |
| Supports | Database structure and relationships |
| Source | [`V1__create_tables.sql`](../src/main/resources/db/migration/V1__create_tables.sql), [`application.yml`](../src/main/resources/application.yml) |
| Verification | Source inspection; H2 execution in EVD-CI-001 |
| Date | Current at baseline |
| Validity | H2 and local MySQL 8.4 execution are verified; other environments remain environment-specific |
| Boundary / next evidence | EVD-SYSTEM-001 supplies MySQL 8.4 runtime evidence; retain CI artifacts after publication |

### EVD-CONTAINER-001: Local container configuration

| Field | Value |
|---|---|
| Description | Multi-stage/non-root application image and two-service Compose |
| Supports | Build stages, health checks, local bind, service dependency, named volume |
| Source | [`Dockerfile`](../Dockerfile), [`compose.yaml`](../compose.yaml) |
| Verification | Source inspection; Docker image built in EVD-CI-001; configuration validation and disposable runtime checks passed in EVD-SYSTEM-001 |
| Date | Current at baseline |
| Validity | Configuration-specific |
| Boundary / next evidence | Start/health/non-root/API/MySQL/restart persistence are verified locally; add backup/restore and target-host evidence |

### EVD-SYSTEM-001: MySQL, Compose, and runtime verification

| Field | Value |
|---|---|
| Description | Disposable two-service runtime verification using MySQL 8.4 and the application image |
| Supports | Compose start ordering/health, Flyway-on-MySQL startup, live API create/search, database persistence, non-root runtime, and named-volume continuity across stop/start |
| Source | [`compose.yaml`](../compose.yaml), [`verify-running-app.sh`](../scripts/verify-running-app.sh), [`verify-volume-persistence.sh`](../scripts/verify-volume-persistence.sh), [pipeline](../.github/workflows/pipeline.yml) |
| Verification | On 2026-08-06, the full sequence exited 0 in WSL under isolated project `secureflow-final-check` on port 18080; health was `UP`; the smoke transaction was present in API search and MySQL; application UID was 100; 21 rows remained before/after Compose stop/start |
| Date | 2026-08-06 |
| Validity | Local Windows 11/WSL/Docker Engine 29.7.1/Compose 5.4.0 result for this branch |
| Boundary / next evidence | The isolated stack and volume were removed after verification; CI must reproduce the checks after publication; this is not backup/restore or target-server evidence |

### EVD-BROWSER-001: Browser and automated accessibility verification

| Field | Value |
|---|---|
| Description | Playwright 1.62.1 Chromium journey and axe-core 4.12.1 WCAG A/AA scan against the MySQL-backed Compose application |
| Supports | Real transaction-form submission, live table refresh, chart selection, and absence of automatically detectable WCAG A/AA violations in the tested page state |
| Source | [`dashboard.spec.js`](../browser-tests/dashboard.spec.js), [`playwright.config.js`](../playwright.config.js), [`package-lock.json`](../package-lock.json) |
| Verification | `npm run test:browser` in the matching Playwright container exited 0 with 2 passed in 6.0 seconds; `npm audit` reported 0 vulnerabilities for the locked browser-test dependency set |
| Date | 2026-08-06 |
| Validity | Chromium/Linux container and tested page state only |
| Boundary / next evidence | Automated axe finds only some accessibility issues; manual keyboard, screen-reader, zoom/reflow, additional states, and other-browser testing remain valuable |

### EVD-PR-044: Manual load-test contribution

| Field | Value |
|---|---|
| Description | k6 scripts and PR-reported gradual/spike results |
| Supports | Existence/configuration of manual load scenarios and historical claim |
| Source | [PR #44](https://github.com/Neueda-Learning/114-Secure-Flow/pull/44), [`load-tests/`](../load-tests/) |
| Verification | Script/PR inspection |
| Date | PR merged 2026-08-06 |
| Validity | Scripts valid for their commit; result validity not independently established |
| Boundary / next evidence | Retain raw output/environment digest, use the current port explicitly, and add an agreed `checks` threshold |

### EVD-GH-001: Issue, PR, branch, review, and workflow inventory

| Field | Value |
|---|---|
| Description | Accessible repository collaboration metadata |
| Supports | States, authors, assignees, branches, open work, review presence, CI history |
| Source | [Repository](https://github.com/Neueda-Learning/114-Secure-Flow), [repository history](repository-history.md) |
| Verification | GitHub connector/API and local Git history/remote reads |
| Date | 2026-08-06 snapshot |
| Validity | Point-in-time; GitHub state can change |
| Boundary / next evidence | EVD-GH-002 records current settings; submitted human reviews remain PR-specific evidence |

### EVD-GH-002: Repository governance and package-access settings

| Field | Value |
|---|---|
| Description | Authenticated GitHub settings inspection for `main`, the container package, and Actions workflow permissions |
| Supports | `main` requires pull requests, one approval, and `test-and-package`; package source/inherited access and repository Admin Actions access are configured; repository workflow default is locked to read-only contents/packages by higher-level policy |
| Source | Repository branch-protection, package, and Actions settings (administrator-visible); [repository workflow](repository-workflow.md), [deployment guide](deployment.md) |
| Verification | Read-only authenticated settings inspection on 2026-08-06; no setting was changed |
| Date | 2026-08-06 |
| Validity | Point-in-time; settings can change |
| Boundary / next evidence | Package publication remains unverified; an organization/enterprise administrator must align workflow-token package-write policy and the team must retain the successful rerun/digest. Add the new system check to branch protection after its first GitHub run. |

### EVD-LOCAL-001: Build tool/dependency inventory

| Field | Value |
|---|---|
| Description | Maven Wrapper and resolved dependency-tree inspection |
| Supports | Maven 3.9.16; local Java 21.0.12; resolved direct/transitive versions listed in technology inventory |
| Source | Reproduce with `.\mvnw.cmd --version` and `.\mvnw.cmd --batch-mode dependency:tree -Dscope=test` |
| Verification | Commands exited 0 on Windows 11 in clean audit worktree |
| Date | 2026-08-06 |
| Validity | Dependency resolution can change if mutable upstream metadata/artifacts change |
| Boundary / next evidence | Retain resolved output where useful and add dedicated license/vulnerability scanning |

### EVD-RUBRIC-001: Mentor rubric photographs

| Field | Value |
|---|---|
| Description | Three clearer photographs showing four evaluation categories and More/Better/Different/Less criteria |
| Supports | Rubric themes transcribed in traceability matrix |
| Source | Supplied privately in the review conversation; not committed |
| Verification | Visual inspection at original resolution |
| Date | Supplied 2026-08-06 |
| Validity | Partial photographic view only |
| Boundary / next evidence | Mentor confirmation of the full source document can complete obscured wording and final ratings |

### EVD-DOC-001: Final branch validation

| Field | Value |
|---|---|
| Description | Documentation consistency checks plus application regression after adding targeted CI/system-test improvements |
| Supports | Relative file/anchor resolution, external-link availability snapshot, terminology/encoding review, scoped change review, Maven/JAR/JaCoCo success, and exact local coverage snapshot |
| Source | Reproducible commands and results documented in [review report](review-report.md#final-quality-validation) |
| Verification | `git diff --check` passed; 219 relative file links and 17 relative anchors resolved; 85 of 86 external links returned HTTP below 400 while the official MySQL manual rejected the automated client with 403; scope/credential-marker checks passed; `./mvnw clean verify` exited 0 with 7 tests, the JaCoCo gate met, and 97.09% report line coverage (334/344) |
| Date | 2026-08-06 |
| Validity | Applies to this documentation change |
| Boundary / next evidence | Point-in-time link and Windows/Java/H2 execution; MySQL/Compose/browser evidence is indexed separately; security, formal accessibility, and target deployment remain separate scopes |

## Evidence expansion queue

The indexed evidence is already broad and reproducible. These additions would
extend it into further operational and governance scopes:

- full mentor rubric source document/text
- independent human approvals for current open PRs
- raw k6 output and benchmark environment
- formal accessibility/security/privacy/legal/licensing reports
- production server configuration, deployment log, backup, restore, or rollback
- exact latest-main JaCoCo percentage copied into repository
- complete historical AI tool/model/prompt record

## Maintenance

Give new evidence a stable identifier, immutable source when possible, commit or
environment, verification method, date only when natural, validity, and
boundaries. Preserve earlier results under their stable IDs when adding newer
evidence.
