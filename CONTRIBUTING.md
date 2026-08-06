# Contributing to SecureFlow

## Purpose and audience

This guide defines the contribution process for developers, reviewers, and
maintainers. Its goal is to keep `main` understandable and runnable while
preserving an honest review trail.

The workflow currently visible in repository history is feature branch -> pull
request -> `main`. Branch protection and mandatory approval rules were not
independently verified during the 2026-08-06 review. Treat the controls in this
guide as team expectations, not proof of GitHub enforcement.

## 1. Start from current main

```bash
git switch main
git pull --ff-only
git switch -c feature/short-description
```

Allowed patterns:

```text
feature/add-reference-filter
fix/alert-status-validation
test/transaction-boundary
docs/explain-flyway
chore/update-dependency
```

Use lowercase words separated by hyphens. Do not include client, employer, bank,
customer, personal, or confidential names in branch names.

## 2. Define scope and evidence before implementation

- Link or create an issue when the change is more than a trivial correction.
- Record the problem, acceptance criteria, risks, dependencies, and expected
  evidence.
- Keep one branch focused on one coherent outcome.
- Separate verified current behavior from proposals and future scope.
- Never create artificial commits, reviews, screenshots, or test results.

See the [repository workflow](docs/repository-workflow.md) for issue, pull
request, release, hotfix, conflict, and review guidance.

## 3. Preserve component boundaries

- Controllers handle HTTP input/output.
- Services contain business rules and transaction boundaries.
- Repositories contain persistence queries.
- Browser code stays in the static assets unless an approved architecture
  decision changes the frontend approach.
- Database schema changes use a new Flyway migration. Never edit an applied
  migration to disguise a later change.
- Do not commit credentials, `.env`, database files, logs, test artifacts, or
  build output.

## 4. Verify the change

Complete verification for normal Java or application changes:

```powershell
.\mvnw.cmd clean verify
```

```bash
./mvnw clean verify
```

For UI changes, run the Playwright suite and manually verify the affected flow
at desktop and narrow viewports. Record the browser and viewport used.
Automated Chromium/axe checks complement manual keyboard, screen-reader,
zoom/reflow, and cross-browser review.

For Docker changes:

```bash
docker compose config
docker compose up --build --wait
curl --fail http://localhost:8080/actuator/health
docker compose down
```

Do not write “all tests passed” unless the command was actually run and its
scope is clear. If a check was not run, write `Not run` and explain why.

## 5. Commit a focused change

Stage only intended files:

```bash
git add -- path/to/file another/path
git diff --cached
git commit -m "docs: add evidence traceability"
```

Common prefixes are `feat`, `fix`, `test`, `docs`, `build`, `refactor`, and
`chore`. Write an imperative subject describing the outcome. Do not rewrite
shared history to improve appearance.

## 6. Open and review a pull request

Complete the repository template. Include:

- summary and motivation
- explicit scope and exclusions
- affected files/components
- related issue or decision
- exact verification command and result
- evidence links
- security, privacy, data, API, migration, and deployment impact
- limitations, risks, rollback/recovery, and reviewer focus

Authors must not mark unchecked work as complete. Reviewers should compare
claims with source, tests, CI, and evidence rather than relying only on the PR
description.

## Definition of done

A change is ready only when applicable items are satisfied:

- acceptance criteria are met and traceable
- code and documentation agree
- tests cover changed behavior, with any next coverage layer documented
- `clean verify` succeeds and the JaCoCo gate remains satisfied
- Docker/configuration checks are complete when affected
- no secrets or generated files are included
- risks and limitations are visible
- required human review is complete
- CI succeeds, including delivery steps that are required for the change

The latest reviewed `main` run demonstrates useful stage-specific evidence:
tests and image construction passed, while registry permissions became the
clearly isolated follow-up action.

## Maintenance

Maintainers should review this guide whenever branch policy, merge strategy,
quality gates, deployment, or repository permissions change. Update
[repository workflow](docs/repository-workflow.md) at the same time.
