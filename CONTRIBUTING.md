# Contributing to SecureFlow

SecureFlow uses `main` for releasable code, `qa` for team integration, and short-lived `feature/*` branches.

## One story, one branch, one pull request

1. Move your assigned story to **In Progress**.
2. Update local `qa`: `git checkout qa` then `git pull origin qa`.
3. Create a branch: `git checkout -b feature/SF-XX-short-name`.
4. Make one small, understandable change and add its tests.
5. Run `.\mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on macOS/Linux.
6. Stage only your files: `git add -- path/to/file1 path/to/file2`.
7. Commit clearly, for example `feat: reject invalid alert transitions`.
8. Push your branch and open a PR into `qa` using the template.
9. A human who did not author the change reviews it. The author explains the code before merge.
10. Move the story to **Done** only after the PR is merged and acceptance evidence is attached.

Never push directly to `qa` or `main`. Never commit credentials or `.env`.

## Daily check-ins

- 09:15 — Yesterday / Today / Blockers.
- 13:00 — five-minute blocker check.
- 16:30 — PR and demo check.

When blocked for more than 30 minutes, write down what you tried and ask a teammate. This is collaboration, not failure.
