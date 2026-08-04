# Contributing to SecureFlow

SecureFlow uses `main` for releasable code and short-lived `feature/*` branches.

## One story, one branch, one pull request

1. Move the assigned story to **In Progress**.
2. Update local `main`: `git switch main` then `git pull --ff-only origin main`.
3. Create a branch: `git switch -c feature/SF-XX-short-name`.
4. Make one focused, understandable change and add or update its tests.
5. Run `.\mvnw.cmd clean verify` on Windows or `./mvnw clean verify` elsewhere.
6. Stage only the intended paths and review `git diff --cached`.
7. Commit clearly, for example `feat: reject invalid alert transitions`.
8. Push the branch and open a pull request into `main`.
9. A teammate who did not author the change reviews it before merge.
10. Move the story to **Done** only after CI and acceptance evidence are green.

Never push directly to `main`. Never commit passwords, `.env`, generated build
output, IDE files, or real customer/payment data.

## Definition of done

- Acceptance criteria are satisfied and linked to the pull request.
- Automated tests cover the behavior and `clean verify` passes.
- At least 70% line coverage remains enforced by the build.
- Relevant API, architecture, deployment, and presentation docs are current.
- UI changes work at desktop and mobile widths.
- A teammate can explain and reproduce the change from a clean checkout.
