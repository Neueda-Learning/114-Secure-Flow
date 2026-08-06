# Agile delivery and Kanban evidence

## Purpose

This document records the repository-verifiable evidence that the SecureFlow
team used a GitHub Projects Kanban board to make work visible, assign ownership,
manage flow, and connect delivery items to repository issues. It gives mentors,
contributors, and auditors a reproducible route from planning evidence to
issues, pull requests, CI, implementation, and tests.

## Scope and current status

- Evidence baseline: `main` commit `13738e3`.
- Board snapshot: authenticated, read-only inspection on 2026-08-06.
- Delivery system: GitHub Issues, pull requests, GitHub Actions, and
  [Team SAFE's Kanban Board](https://github.com/orgs/Neueda-Learning/projects/18).
- Board visibility: private organization project. An evaluator needs authorized
  `Neueda-Learning` access to open the board; the linked repository issues and
  pull requests remain the independently navigable evidence layer.
- Evidence identifier: [`EVD-AGILE-001`](evidence-index.md#evd-agile-001-github-projects-kanban-delivery-record).

This is evidence of a Kanban-style delivery workflow. It is not presented as
proof of Scrum ceremonies, sprint velocity, stand-ups, retrospectives, or a
particular Agile certification because those records were not available in the
repository review.

## Board identity and configuration

| Item | Verified observation |
|---|---|
| Organization project | `Team SAFE's Kanban Board`, project `#18` |
| Repository project listing | One open project and no closed projects were visible for the repository |
| Flow columns | `Backlog`, `Ready`, `In progress`, `In review`, `Done`, and `BLOCKED` |
| Column descriptions | Backlog: not started; Ready: ready to be picked up; In progress: actively worked on; In review: under review; Done: completed |
| Views | `Backlog`, `Priority board`, `Team items`, `Roadmap`, and `My items` |
| Automation | GitHub displayed `7 enabled` project workflows |
| Insight views | Default `Burn up` chart plus a custom `Status chart` |
| Tracking fields | Title, Status, Linked pull requests, Sub-issues progress, Size, Estimate, Priority, Start date, and Target date |

The Priority board grouped one item as Urgent and 17 as No Priority at the
snapshot. This confirms that the board supports prioritization while preserving
the exact extent to which the team populated that field.

The workflow screen listed automation templates for adding project items and
sub-issues, closing/reopening items, responding to linked or merged pull
requests, review outcomes, and archiving. The aggregate `7 enabled` value is
verified; this document does not infer that every listed template was enabled.

## Work-flow model

```text
Backlog -> Ready -> In progress -> In review -> Done
                         |
                         +-> BLOCKED -> return to the active flow
```

This structure provides visible intake, an explicit ready queue, active-work
and review stages, a completion state, and a separate place to expose blockers.
Configured column indicators displayed `1 / 5` for Backlog, `0 / 3` for In
progress, and `0 / 5` for In review at inspection time. These are useful
work-in-progress signals; the snapshot does not independently prove how the
team enforced them outside GitHub.

## Point-in-time delivery evidence

The Backlog and Status chart displayed 18 matching issue items:

| Status | Item count on 2026-08-06 | Evidence |
|---|---:|---|
| Backlog | 1 | Issue [#42](https://github.com/Neueda-Learning/114-Secure-Flow/issues/42), assigned to `RushilAlagh` |
| Ready | 0 | Authenticated board snapshot |
| In progress | 0 | Authenticated board snapshot |
| In review | 0 | Authenticated board snapshot |
| Done | 17 | Authenticated board and custom Status chart |
| Blocked | 0 | Authenticated board snapshot |

The burn-up chart supplied dated flow evidence for its 18 issue items. It showed
completed work increasing from 0 on 2026-07-29 to 1 on 2026-08-02, 5 on
2026-08-03, 13 on 2026-08-04, and 17 on 2026-08-06. The open series showed one
item on 2026-08-06. These values demonstrate incremental movement in the board;
they are not used as a velocity or forecasting claim.

The Roadmap view was configured with month zoom and selectable date fields.
No dated item bars were visible in the inspected state. Start/target date and
estimate fields are therefore a ready extension point for future forecasting,
not evidence of completed schedule estimation.

## Planning-to-delivery traceability

The board's 18 items correspond to the public issue record summarized below.
The [repository history](repository-history.md) supplies the related pull
requests and preserves authorship and outcome.

| Delivery area | Board/issue evidence | Repository outcome evidence |
|---|---|---|
| Application foundation | [#1](https://github.com/Neueda-Learning/114-Secure-Flow/issues/1) Spring Boot foundation | PR [#2](https://github.com/Neueda-Learning/114-Secure-Flow/pull/2) |
| Data model and API | [#3](https://github.com/Neueda-Learning/114-Secure-Flow/issues/3), [#10](https://github.com/Neueda-Learning/114-Secure-Flow/issues/10) | PRs [#8](https://github.com/Neueda-Learning/114-Secure-Flow/pull/8), [#13](https://github.com/Neueda-Learning/114-Secure-Flow/pull/13) |
| Monitoring configuration and engine | [#4](https://github.com/Neueda-Learning/114-Secure-Flow/issues/4), [#11](https://github.com/Neueda-Learning/114-Secure-Flow/issues/11), [#16](https://github.com/Neueda-Learning/114-Secure-Flow/issues/16), [#23](https://github.com/Neueda-Learning/114-Secure-Flow/issues/23) | PRs [#7](https://github.com/Neueda-Learning/114-Secure-Flow/pull/7), [#18](https://github.com/Neueda-Learning/114-Secure-Flow/pull/18), [#26](https://github.com/Neueda-Learning/114-Secure-Flow/pull/26), [#27](https://github.com/Neueda-Learning/114-Secure-Flow/pull/27) |
| Dashboard and transaction form | [#5](https://github.com/Neueda-Learning/114-Secure-Flow/issues/5), [#12](https://github.com/Neueda-Learning/114-Secure-Flow/issues/12), [#22](https://github.com/Neueda-Learning/114-Secure-Flow/issues/22) | PRs [#6](https://github.com/Neueda-Learning/114-Secure-Flow/pull/6), [#14](https://github.com/Neueda-Learning/114-Secure-Flow/pull/14), [#29](https://github.com/Neueda-Learning/114-Secure-Flow/pull/29) |
| Alert lifecycle | [#15](https://github.com/Neueda-Learning/114-Secure-Flow/issues/15) | PR [#27](https://github.com/Neueda-Learning/114-Secure-Flow/pull/27) and current rewrite history |
| Shared responses and delivery | [#17](https://github.com/Neueda-Learning/114-Secure-Flow/issues/17), [#21](https://github.com/Neueda-Learning/114-Secure-Flow/issues/21) | PRs [#25](https://github.com/Neueda-Learning/114-Secure-Flow/pull/25), [#27](https://github.com/Neueda-Learning/114-Secure-Flow/pull/27) |
| Small team improvements | [#37](https://github.com/Neueda-Learning/114-Secure-Flow/issues/37), [#38](https://github.com/Neueda-Learning/114-Secure-Flow/issues/38), [#39](https://github.com/Neueda-Learning/114-Secure-Flow/issues/39), [#43](https://github.com/Neueda-Learning/114-Secure-Flow/issues/43) | PRs [#40](https://github.com/Neueda-Learning/114-Secure-Flow/pull/40), [#44](https://github.com/Neueda-Learning/114-Secure-Flow/pull/44), [#41](https://github.com/Neueda-Learning/114-Secure-Flow/pull/41), [#45](https://github.com/Neueda-Learning/114-Secure-Flow/pull/45) |
| Prioritized open improvement | [#42](https://github.com/Neueda-Learning/114-Secure-Flow/issues/42) two-column chart layout | Open chart work is documented in [repository history](repository-history.md#current-baseline-and-open-work) |

Representative completed cards retain different assignees—`rudracodeshere`,
`RushilAlagh`, `Sriramchannamsetty`, and `Minal724`—which supports visible team
ownership. Git history and PR authorship remain the authoritative evidence for
who implemented and committed each outcome.

## Evidence chain and Agile value

| Practice | Evidence chain | What is supported |
|---|---|---|
| Visible work | Kanban status columns -> board cards -> issues | Work intake, current state, blockers, and completion are visible to authorized project members |
| Incremental delivery | 18 scoped issues -> focused PRs -> burn-up movement | The project was decomposed and delivered in multiple increments rather than one undifferentiated change |
| Ownership | Board assignees -> issue assignees -> PR authors -> Git commits | Responsibility can be traced without replacing authentic authorship |
| Review stage | `In review` column -> linked-PR field -> PR records | Review is represented in the delivery model; submitted approvals remain PR-specific evidence |
| Fast feedback | PR -> GitHub Actions -> Maven/system/browser jobs | Automated feedback is connected to proposed repository changes |
| Prioritization | Priority board -> urgent/no-priority groups -> Backlog/Ready states | The board supports explicit priority and readiness decisions |
| Progress inspection | Burn-up and Status charts | Current completion distribution and dated item movement can be inspected |

## Evidence boundaries and solution path

The evidence is strong for visible work decomposition, assignment, status flow,
incremental completion, and repository linkage. The following boundaries keep
the record precise and identify constructive next steps:

- The board is private. Keep the project link and public issue/PR cross-links
  current, and arrange authorized evaluator access when board inspection is
  required.
- The snapshot records current state and the burn-up series, not every historic
  card transition. GitHub's issue/PR event history can be retained for any
  future cycle-time analysis.
- `Done` records board completion; acceptance, test, and deployment claims must
  still use the linked PR and CI evidence.
- Estimates and date fields were present but not populated in the visible
  snapshot. Populate them only when the team will maintain them and use them for
  decisions.
- Ceremony notes were not part of the inspected repository evidence. Brief,
  dated demo/retro/decision notes can complement the already strong delivery
  trail when those activities occur.

## Rubric relevance

This evidence directly supports repository-verifiable parts of the mentor
criteria for Git discipline, PR-first incremental work, keeping work visible,
feedback loops, scope management, automation, and shared ownership. The
[rubric traceability matrix](traceability-matrix.md#mentor-rubric-traceability)
keeps behavioral criteria partially verified until mentor/team observation is
available.

## Maintenance guidance

After material planning changes, the project owner should update the snapshot
date, current counts, workflow/view configuration, and linked open work. Never
replace a historical observation with a newer value without changing the date.
Keep board status, issue state, PR outcome, CI result, and implementation status
as separate evidence layers.

## Related documents

- [Documentation index](README.md)
- [Repository workflow](repository-workflow.md)
- [Repository history](repository-history.md)
- [Evidence index](evidence-index.md)
- [Traceability matrix](traceability-matrix.md)
- [Mentor evidence guide](mentor-review-guide.md)
- [Testing and CI evidence](testing.md)

## Official platform references

- [About GitHub Projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects/learning-about-projects/about-projects)
- [Changing the layout of a project view](https://docs.github.com/en/issues/planning-and-tracking-with-projects/customizing-views-in-your-project/changing-the-layout-of-a-view)
- [Automating a GitHub Project](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project)
- [Viewing project insights](https://docs.github.com/en/issues/planning-and-tracking-with-projects/viewing-insights-from-your-project)
