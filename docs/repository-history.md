# Repository history and collaboration evidence

## Purpose, scope, and snapshot

This document inventories accessible GitHub issues, pull requests, remote
branches, reviews, and CI as observed on 2026-08-06. It supports traceability
without rewriting historical descriptions. A historical PR claim applies to
that PR/commit and is not automatically evidence for current `main`.

Repository: [Neueda-Learning/114-Secure-Flow](https://github.com/Neueda-Learning/114-Secure-Flow)

## Project board snapshot

The repository is connected to the private organization project
[Team SAFE's Kanban Board](https://github.com/orgs/Neueda-Learning/projects/18).
Authenticated read-only inspection on 2026-08-06 found 18 linked issue items:
17 Done and one Backlog. The board defines Backlog, Ready, In progress, In
review, Done, and Blocked states, exposes five planning views, displays seven
enabled workflows, and includes burn-up and status charts.

This board evidence strengthens the issue/PR inventory with planning state,
ownership, WIP signals, and incremental completion. Because the project is
private, evaluators need authorized organization access; the repository's
public issue and PR links provide the complementary evidence trail. See
[Agile delivery and Kanban evidence](agile-delivery-evidence.md) and
[`EVD-AGILE-001`](evidence-index.md#evd-agile-001-github-projects-kanban-delivery-record).

## Current baseline and open work

- `main`: commit `13738e3`; tests, coverage, artifacts, MySQL/Compose/browser
  checks, image construction, and GHCR publication passed in run 31098653366.
- PR [#48](https://github.com/Neueda-Learning/114-Secure-Flow/pull/48):
  merged the audit documentation and system verification. CI passed; the merge
  used an authorized administrator bypass while the required independent review
  was still pending, so no reviewer approval is claimed.
- Issue [#42](https://github.com/Neueda-Learning/114-Secure-Flow/issues/42):
  open; chart two-column layout; assigned to `RushilAlagh`.
- PR [#46](https://github.com/Neueda-Learning/114-Secure-Flow/pull/46):
  open draft; Linux Docker CD; CI passed and the deploy job correctly skipped on
  the PR event; environment prerequisites and supervised deployment are next.
- PR [#47](https://github.com/Neueda-Learning/114-Secure-Flow/pull/47):
  open and non-draft; chart defaults/colors/legend; CI passed and review was
  requested, with submitted review as the next collaboration step.

## Issue inventory

| Issue | Title | State | Assignee | Related outcome/status |
|---|---|---|---|---|
| [#1](https://github.com/Neueda-Learning/114-Secure-Flow/issues/1) | Spring Boot foundation | Closed | `rudracodeshere` | PR #2 merged |
| [#3](https://github.com/Neueda-Learning/114-Secure-Flow/issues/3) | Transaction data model | Closed | `rudracodeshere` | Day-one integration history |
| [#4](https://github.com/Neueda-Learning/114-Secure-Flow/issues/4) | Monitoring rule configuration | Closed | `Sriramchannamsetty` | PR #7 merged |
| [#5](https://github.com/Neueda-Learning/114-Secure-Flow/issues/5) | Dashboard shell | Closed | `RushilAlagh` | PR #6 merged |
| [#10](https://github.com/Neueda-Learning/114-Secure-Flow/issues/10) | Transaction API | Closed | `rudracodeshere` | PR #13 merged |
| [#11](https://github.com/Neueda-Learning/114-Secure-Flow/issues/11) | Amount monitoring rule | Closed | `Sriramchannamsetty` | PR #18 merged |
| [#12](https://github.com/Neueda-Learning/114-Secure-Flow/issues/12) | Transaction form | Closed | `RushilAlagh` | PR #14 merged; PR body references #7 instead of #12 |
| [#15](https://github.com/Neueda-Learning/114-Secure-Flow/issues/15) | Alert lifecycle/history | Closed | `rudracodeshere` | PR #27 and later rewrite history |
| [#16](https://github.com/Neueda-Learning/114-Secure-Flow/issues/16) | New-payee and velocity monitoring | Closed | `rudracodeshere` | PR #27 and later rewrite history |
| [#17](https://github.com/Neueda-Learning/114-Secure-Flow/issues/17) | Dashboard/tests/delivery | Closed | `rudracodeshere` | PR #27/#29 and later rewrite history |
| [#21](https://github.com/Neueda-Learning/114-Secure-Flow/issues/21) | Shared API response models | Closed | `Minal724` | PR #25 merged |
| [#22](https://github.com/Neueda-Learning/114-Secure-Flow/issues/22) | Dashboard and delivery | Closed | `RushilAlagh` | PR #29 merged |
| [#23](https://github.com/Neueda-Learning/114-Secure-Flow/issues/23) | Monitoring engine/rules API | Closed | `Sriramchannamsetty` | PR #26 merged |
| [#37](https://github.com/Neueda-Learning/114-Secure-Flow/issues/37) | Auto-hide generated-alert notification | Closed | `Sriramchannamsetty` | PR #40 merged |
| [#38](https://github.com/Neueda-Learning/114-Secure-Flow/issues/38) | Test 1,000 transactions | Closed | `RushilAlagh` | PR #44 delivered manual k6 test, which differs from issue wording “automated test” |
| [#39](https://github.com/Neueda-Learning/114-Secure-Flow/issues/39) | Favicon | Closed | `Minal724` | PR #41 merged |
| [#42](https://github.com/Neueda-Learning/114-Secure-Flow/issues/42) | Two-column charts | Open | `RushilAlagh` | PR #47 provides related chart-presentation work; the two-column acceptance criterion remains the next scoped step |
| [#43](https://github.com/Neueda-Learning/114-Secure-Flow/issues/43) | Chart Explorer sidebar | Closed | `rudracodeshere` | PR #45 merged |

Early title-only issues are preserved as authentic history. The current
requirements document adds present-day detail without changing past context.

## Pull-request inventory

| PR | Author | Outcome | Historical scope note |
|---|---|---|---|
| [#2](https://github.com/Neueda-Learning/114-Secure-Flow/pull/2) | `rudracodeshere` | Merged | Spring foundation |
| [#6](https://github.com/Neueda-Learning/114-Secure-Flow/pull/6) | `RushilAlagh` | Merged | Dashboard shell |
| [#7](https://github.com/Neueda-Learning/114-Secure-Flow/pull/7) | `Sriramchannamsetty` | Merged | Rule configuration |
| [#8](https://github.com/Neueda-Learning/114-Secure-Flow/pull/8) | `rudracodeshere` | Merged | Day-one integration |
| [#9](https://github.com/Neueda-Learning/114-Secure-Flow/pull/9) | `rudracodeshere` | Merged | Main workflow simplification |
| [#13](https://github.com/Neueda-Learning/114-Secure-Flow/pull/13) | `rudracodeshere` | Merged | Transaction API |
| [#14](https://github.com/Neueda-Learning/114-Secure-Flow/pull/14) | `RushilAlagh` | Merged | Transaction form; historical issue link appears inconsistent |
| [#18](https://github.com/Neueda-Learning/114-Secure-Flow/pull/18) | `Sriramchannamsetty` | Merged | Amount rule |
| [#19](https://github.com/Neueda-Learning/114-Secure-Flow/pull/19) | `rudracodeshere` | Closed, unmerged | Monitoring MVP proposal |
| [#20](https://github.com/Neueda-Learning/114-Secure-Flow/pull/20) | `rudracodeshere` | Closed draft, unmerged | Reference proposal |
| [#24](https://github.com/Neueda-Learning/114-Secure-Flow/pull/24) | `Minal724` | Closed, unmerged | Dashboard UI test proposal |
| [#25](https://github.com/Neueda-Learning/114-Secure-Flow/pull/25) | `Minal724` | Merged | Shared API models |
| [#26](https://github.com/Neueda-Learning/114-Secure-Flow/pull/26) | `Sriramchannamsetty` | Merged | Monitoring engine/rules API |
| [#27](https://github.com/Neueda-Learning/114-Secure-Flow/pull/27) | `rudracodeshere` | Merged | Backend integration |
| [#28](https://github.com/Neueda-Learning/114-Secure-Flow/pull/28) | `rudracodeshere` | Closed draft, unmerged | Dashboard/delivery proposal |
| [#29](https://github.com/Neueda-Learning/114-Secure-Flow/pull/29) | `RushilAlagh` | Merged | Dashboard/delivery |
| [#30](https://github.com/Neueda-Learning/114-Secure-Flow/pull/30) | `rudracodeshere` | Merged | Historical UI redesign |
| [#31](https://github.com/Neueda-Learning/114-Secure-Flow/pull/31) | `rudracodeshere` | Merged | Historical Linux/docs delivery |
| [#32](https://github.com/Neueda-Learning/114-Secure-Flow/pull/32) | `rudracodeshere` | Merged | Historical product/deployment state |
| [#33](https://github.com/Neueda-Learning/114-Secure-Flow/pull/33) | `RushilAlagh` | Merged | Chart Explorer, authorship preserved |
| [#34](https://github.com/Neueda-Learning/114-Secure-Flow/pull/34) | `rudracodeshere` | Merged | Beginner-friendly implementation replacement |
| [#35](https://github.com/Neueda-Learning/114-Secure-Flow/pull/35) | `rudracodeshere` | Closed, unmerged | Dashboard/demo proposal |
| [#36](https://github.com/Neueda-Learning/114-Secure-Flow/pull/36) | `rudracodeshere` | Merged | Current dashboard/demo foundation |
| [#40](https://github.com/Neueda-Learning/114-Secure-Flow/pull/40) | `Sriramchannamsetty` | Merged | Popup timeout; repository history adds context to the concise description |
| [#41](https://github.com/Neueda-Learning/114-Secure-Flow/pull/41) | `Minal724` | Merged | Favicon |
| [#44](https://github.com/Neueda-Learning/114-Secure-Flow/pull/44) | `RushilAlagh` | Merged | Manual k6 scripts with reported results; retained raw artifacts are the next evidence enhancement |
| [#45](https://github.com/Neueda-Learning/114-Secure-Flow/pull/45) | `rudracodeshere` | Merged | Chart sidebar link |
| [#46](https://github.com/Neueda-Learning/114-Secure-Flow/pull/46) | `rudracodeshere` | Open draft | Proposed Linux CD; CI passed and supervised deployment/review remain before merge |
| [#47](https://github.com/Neueda-Learning/114-Secure-Flow/pull/47) | `RushilAlagh` | Open | Proposed chart presentation changes |

Historical PRs report differing test counts, architectures, UI scope, and
deployment results because the application was later replaced/refactored.
Reviewers should use the current source and evidence matrix for present-state
claims.

## Branch inventory

Remote branches visible at snapshot:

```text
main
feature/SF-08-monitoring-mvp
feature/SF-12-dashboard-delivery
feature/SF-13-monitoring-engine
feature/SF-16-ui-redesign
feature/SF-17-linux-vm-deployment
feature/SF-37-Add-timeout-alerts
feature/SF-39-favicon-pipe-mark
feature/chart-explorer-sidebar
feature/chart-explorer-ui-20260805
feature/chart-severity-default-colorful
feature/final-live-inr-product
feature/linux-docker-cd
feature/manual-load-tests
release/beginner-main-20260805
```

The inventory enables safe, owner-confirmed cleanup of merged or obsolete
branches. Confirming protection and automatic-deletion settings will add useful
governance evidence.

## Contributor evidence

Git shortlog shows commits attributed to Rudra Sharma, Rushil/RushilAlagh,
Minal/Minal724, and Sriram/Sriramchannamsetty using multiple email identities.
This provides a transparent authorship map; PR, test, and team evidence can
complement it when assessing individual contribution quality.

## Documentation review decision

Historical issues and merged PR bodies retain their authentic context. This
snapshot, the Kanban record, requirements, evidence, risk, and traceability
network supply the current interpretation. Open items can be strengthened
through clearly dated updates that preserve original evidence.

## Maintenance

Update this inventory only from fresh GitHub reads. Record the snapshot date,
preserve authorship/state, and distinguish merged, closed-unmerged, draft, and
open work.
