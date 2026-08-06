# Repository workflow

## Purpose and scope

This document records the workflow evidenced in repository history and provides
a clear path to stronger governance.

## Current evidenced workflow

- Default branch: `main`.
- Work is commonly developed on `feature/...` branches.
- Pull requests generally target `main`.
- GitHub Actions runs for every pull request and push to `main`.
- A private GitHub Projects Kanban board connects 18 repository issues to
  Backlog, Ready, In progress, In review, Done, and Blocked states. Its board,
  priority, team, roadmap, and personal views make intake, ownership, flow, and
  progress available to authorized project members.
- History contains merge commits, direct feature commits, closed unmerged PRs,
  draft PRs, and long-lived remote branches.
- `pom.xml` provides an artifact version; annotated Git releases are the next
  release-governance layer.
- GitHub settings inspection on 2026-08-06 verified that `main` requires pull
  requests, one approval, and the `test-and-package` status check, while force
  pushes and deletion remain disabled.
- PR #48 passed its required CI but was merged using an authorized administrator
  bypass while its independent review remained pending. This is an exception,
  not approval evidence; future protected merges should follow the normal rule.
- PR #47 already requested a reviewer; submitted review records are the next
  visible collaboration evidence for the open PRs.

See [Agile delivery and Kanban evidence](agile-delivery-evidence.md) for the
authenticated board snapshot and [repository history](repository-history.md)
for the dated issue/PR inventory.

## Kanban planning and delivery

The evidenced project workflow uses the board as the planning layer and GitHub
Issues, pull requests, and Actions as the durable delivery layer:

```text
Board item -> assigned issue -> feature branch -> pull request
           -> review and CI -> merge -> Done
```

At the 2026-08-06 snapshot, the board showed 17 Done items and one Backlog item,
plus a burn-up chart recording incremental completion. It also displayed seven
enabled project workflows and WIP indicators for active columns. Board status
does not replace acceptance or testing evidence; those claims are established
by the linked PR, source, tests, and CI run.

The project is private, so maintainers should preserve public issue/PR links and
arrange authorized board access for evaluators who need to inspect the planning
layer.

## Branch naming convention

Use:

```text
feature/short-outcome
fix/short-problem
test/short-scenario
docs/short-topic
chore/short-maintenance
```

Names should be lowercase, short, and free of client, employer, customer,
personal, or confidential identifiers. Existing historical branches are not
renamed because that would alter or confuse the audit trail.

## Branch lifecycle

1. Select a Ready board item or agree a clearly scoped issue from Backlog.
2. Create from current `main` and link the issue.
3. Make focused commits with truthful messages.
4. Rebase or merge current `main` only when needed; do not rewrite shared
   history without team agreement.
5. Open one PR for the outcome and complete the template.
6. Move the item through active/review states and resolve review and CI findings.
7. Merge using the repository-approved strategy and confirm the board outcome.
8. Delete the remote branch after merge when no longer needed.

The historical branch inventory supports owner-confirmed cleanup and future
automatic deletion after merge.

## Pull-request workflow

### Author

- describe motivation, scope, files, tests, evidence, risks, limitations,
  security/privacy impact, and recovery
- distinguish `Not run`, `Not applicable`, and `Passed`
- link the issue using `Closes #N` only when the PR actually satisfies it
- keep screenshots optional and purposeful
- never use the PR body itself as the only evidence of a manual test

### Reviewer

- inspect source and configuration, not only screenshots
- compare acceptance criteria with test/evidence coverage
- verify current/future-state language and data-safety implications
- request changes for unsupported claims, failing required CI, secrets, or
  undocumented migrations
- record approval or review findings in GitHub when possible

### Merge

Merge after every applicable build, delivery, migration, and security gate is
satisfied. Draft PR #46 is a useful example of keeping environment prerequisites
visible until supervised verification is complete.

## Issue workflow

An issue should include problem/context, scope, acceptance criteria,
reproduction/evidence, technical references, risks/dependencies, related work,
and status. Use the repository templates for new work. Historical empty issues
are retained as historical records and are not retroactively expanded.

## Conflict resolution

1. Fetch the remote and inspect divergence.
2. Identify which requirement each side implements.
3. Resolve in the feature branch, preserving both valid changes where possible.
4. Run the complete applicable checks.
5. Explain non-obvious resolution decisions in the PR.
6. Never use destructive reset/force-push on another contributor's work without
   explicit coordination.

## Releases and versioning

Current `pom.xml` reports version `1.0.0`, providing an artifact-versioning
foundation. A reviewed Git tag/release will promote it into formal release
evidence when owners are ready.

Recommended future practice:

- adopt semantic versioning after product-owner confirmation
- create annotated tags only from reviewed, green `main`
- publish release notes that link requirements, PRs, migrations, risks, and
  artifacts
- avoid treating mutable `:latest` as the only deployable identity

## Hotfix process

The following recommended process formalizes hotfix handling:

1. create `fix/...` from the affected released tag/branch
2. document impact and urgency
3. add a regression test
4. follow normal review and CI
5. merge forward into active development
6. create a new version/tag; do not overwrite an existing release

Database migrations remain linear and must also be included in later versions.

## Protected-branch maturity recommendations

The verified rule already requires a pull request, one approval, and
`test-and-package`. The revised system workflow has now produced a successful
main run, so repository owners can extend the rule:

- target `main` with one ruleset or branch-protection rule
- require pull requests and at least one independent approval
- keep the verified `test-and-package` check required and consider requiring
  the now-proven `MySQL, Compose and browser checks` job
- dismiss stale approvals after material changes and require approval of the
  most recent reviewable push by someone other than its author
- prevent force pushes and branch deletion
- require conversation resolution
- restrict direct pushes and deployment-environment access
- add CODEOWNERS only after owners agree responsibility

These controls require repository administration or an equivalent rules role;
documentation and workflow files cannot enforce them by themselves. GitHub's
[protected-branch guidance](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
defines the available review and status-check controls. Record the actual
enabled settings only after an administrator confirms them.

## Maintenance

The repository owner should periodically reconfirm merge strategy, required
checks, release ownership, and hotfix authority. Record changed settings here
and link screenshots/API output only if they add reproducible value.
