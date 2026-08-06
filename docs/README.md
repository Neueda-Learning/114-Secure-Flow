# Documentation index

## Purpose

This index is the reviewer and maintainer entry point for SecureFlow. It maps
each question to one authoritative document so evidence and claims are not
duplicated across unrelated files.

## Review path

1. [Project overview](project-overview.md) — purpose, scope, stakeholders, and
   current status.
2. [Requirements](requirements.md) — uniquely identified functional and
   quality requirements.
3. [Architecture](architecture.md) and [API reference](api.md) — implementation
   and data flow.
4. [Supplementary technology guide](supplementary-technology-guide.md) —
   beginner explanations of Flyway, MockMvc, JaCoCo, k6, and related project
   tooling.
5. [Testing](testing.md) and [evidence index](evidence-index.md) — what is
   verified, how, and which evidence layer comes next.
6. [Traceability matrix](traceability-matrix.md) — requirement and rubric
   mapping.
7. [Mentor rubric evidence guide](mentor-review-guide.md) — concise review and
   presentation path across all four rubric areas.
8. [Agile delivery and Kanban evidence](agile-delivery-evidence.md) — planning,
   ownership, flow, burn-up, and issue/PR traceability.
9. [Security and threat model](security-and-threat-model.md),
   [India privacy/compliance considerations](privacy-compliance-india.md), and
   [risk register](risk-register.md).
10. [Repository review report](review-report.md) — audit outcome and next
   actions.

## Documentation map

| Document | Primary audience | Authoritative content | Status |
|---|---|---|---|
| [README](../README.md) | New users | Quick start and honest status summary | Current |
| [Project overview](project-overview.md) | Mentors, owners, reviewers | Purpose, scope, users, assumptions | Current |
| [Requirements](requirements.md) | Product owner, developers, testers | Requirement identifiers and acceptance evidence | Current reconstruction |
| [Architecture](architecture.md) | Developers, reviewers | Components, data flow, database, boundaries | Current reconstruction |
| [Architecture decisions](decisions/README.md) | Maintainers | Decision records and review conditions | Current reconstruction |
| [API reference](api.md) | API users, testers | Endpoint behavior and error model | Current |
| [Development guide](development.md) | Developers | Local setup, configuration, build, debugging | Current |
| [Deployment guide](deployment.md) | Operators, developers | Current local Compose operation and delivery boundary | Current |
| [Testing](testing.md) | Developers, reviewers | Strategy, commands, results, and coverage roadmap | Current |
| [Technology inventory](technology-inventory.md) | Maintainers, security reviewers | Direct technologies, versions, purpose, considerations, links | Current snapshot |
| [Supplementary technology guide](supplementary-technology-guide.md) | Learners, mentors, presenters | Beginner explanations, real code/configuration, runtime timing, and pass/fail rules for project-specific tools | Current; course classification partly owner-provided |
| [Repository workflow](repository-workflow.md) | Contributors, maintainers | Actual workflow and recommendations | Current snapshot |
| [Repository history](repository-history.md) | Auditors, mentors | Accessible issues, PRs, branches, and CI summary | Current snapshot |
| [Agile delivery and Kanban evidence](agile-delivery-evidence.md) | Mentors, contributors, auditors | Private GitHub Project configuration, point-in-time flow evidence, burn-up, ownership, and public issue/PR links | Current snapshot; authorized board access required |
| [Evidence index](evidence-index.md) | Auditors, reviewers | Evidence identifiers, validity, and next evidence steps | Current snapshot |
| [Traceability matrix](traceability-matrix.md) | Mentors, owners | Requirements/rubric to evidence mapping | Current snapshot |
| [Mentor rubric evidence guide](mentor-review-guide.md) | Mentors, contributors | Concise evidence walkthrough and presentation checklist | Current snapshot |
| [Security and threat model](security-and-threat-model.md) | Security reviewers, owners | Assets, threats, controls, and maturity plan | Current assessment |
| [Root security policy](../SECURITY.md) | Reporters | Vulnerability reporting | Current |
| [India privacy/compliance](privacy-compliance-india.md) | Owners, legal/security reviewers | Potential legal relevance and next-stage controls | Informational; legal review required |
| [AI-assistance record](ai-usage.md) | Reviewers, owners | Transparent AI support disclosure | Current known record |
| [Risk register](risk-register.md) | Owners, maintainers | Prioritized safeguards and solution paths | Current assessment |
| [Current boundaries](known-limitations.md) | All stakeholders | Verified scope and improvement roadmap | Current |
| [Future scope](future-scope.md) | Owners, planners | Proposed work separated from implementation | Planned only |
| [Repository review report](review-report.md) | Mentors, owners | Final review findings and coverage | Current snapshot |
| [Contributing](../CONTRIBUTING.md) | Contributors | Day-to-day contribution checklist | Current |
| [API examples](api-examples.http) | Developers, testers | Executable HTTP examples | Current |
| [Manual load tests](../load-tests/README.md) | Performance testers | Manual k6 scenarios and evidence method | Current; reproducibility enhancement planned |

## Status language

Documents use these terms consistently:

- **Verified** — direct source, test, or persistent CI evidence supports the
  claim.
- **Partially verified** — a direct evidence foundation exists and an additional
  scope/environment check is identified.
- **Documented but not verified** — the process is clearly described and its
  independent verification is the next step.
- **Not implemented** — the capability is explicitly reserved for a later stage.
- **Planned** — proposed work that is not part of the reviewed baseline.
- **Requires clarification** — project-owner or source-rubric confirmation is
  required.

## Baseline and maintenance

Unless stated otherwise, snapshot documents describe `main` commit `13738e3`
as reviewed on 2026-08-06. Open and draft pull requests are not treated as
implemented. Maintainers should update the index, requirements, evidence,
traceability, risks, and review report together after material changes.
