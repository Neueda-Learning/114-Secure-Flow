# Contributing to SecureFlow

The project uses a small pull-request workflow so main remains runnable.

## 1. Start from current main

~~~bash
git switch main
git pull --ff-only
git switch -c feature/short-description
~~~

Use a focused branch name such as:

~~~text
feature/add-reference-filter
fix/alert-status-validation
docs/explain-flyway
~~~

## 2. Keep changes simple

- Prefer direct, readable code over new abstractions.
- Keep controllers responsible for HTTP input/output.
- Keep business decisions in services.
- Keep database access in repositories.
- Add a new Flyway migration for schema changes.
- Do not edit a migration that has already been used.
- Never commit passwords, tokens, .env files, database files, or build output.

## 3. Run the complete local check

Windows:

~~~powershell
.\mvnw.cmd clean verify
~~~

Linux or macOS:

~~~bash
chmod +x mvnw
./mvnw clean verify
~~~

Do not open a pull request until the build prints **BUILD SUCCESS**.

For frontend changes, also open the dashboard and verify the affected flow at
desktop and narrow widths.

For Docker changes:

~~~bash
docker compose config
docker compose up --build --wait
curl --fail http://localhost:8080/actuator/health
docker compose down
~~~

## 4. Commit a focused change

~~~bash
git add -- path/to/changed-file
git commit -m "feat: describe the user-visible change"
git push -u origin feature/short-description
~~~

Common commit prefixes:

- **feat:** new behavior
- **fix:** corrected behavior
- **test:** test-only change
- **docs:** documentation-only change
- **build:** Maven, Docker, or CI change
- **refactor:** code change with no intended behavior change

## 5. Open a pull request

Complete the pull-request template. The pull request must explain:

- what changed
- why it changed
- how it was tested
- whether the API, database, or deployment changed

Wait for the GitHub Actions workflow to pass before merging. Do not bypass a
failed test or coverage check.

## Definition of done

A change is ready to merge when:

- code is understandable without hidden assumptions
- tests cover the new or corrected behavior
- **mvnw clean verify** succeeds
- coverage remains at or above 70%
- Docker-related changes are validated
- documentation matches the final behavior
- no credentials or generated files are included
