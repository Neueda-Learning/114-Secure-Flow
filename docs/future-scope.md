# Future scope

## Purpose and prioritization rule

This document keeps proposed capabilities clearly separated from the verified
baseline. A proposal becomes implemented when requirements, merged source,
tests, and evidence agree. Owners can prioritize by user value and risk reduction.

## Near-term foundations

| Area | Proposed outcome | Preconditions/evidence for completion |
|---|---|---|
| CI delivery | Align GHCR ownership/permissions and publish immutable SHA/digest | Green main run, package/digest link, pull verification |
| Repository governance | Extend verified required-PR/one-approval protection with the new system check, stale-approval dismissal, conversation resolution, and branch cleanup policy | Updated settings evidence and retained PR approval |
| Licensing | Select project license and produce dependency/container license inventory | Qualified owner/legal confirmation |
| Test parity | Extend the implemented MySQL/Compose system check with targeted Testcontainers cases only where database-specific code requires them | Reproducible edge-case result and documented runtime cost |
| Security scanning | Add secret, dependency, source, container, and configuration scans | Triage owner, severity policy, baseline and failure rules |
| Load evidence | Align default port/check threshold and retain immutable k6 methodology/output | Controlled rerun with environment/image digests and results |
| Documentation maintenance | Automate relative-link/Markdown/terminology checks | CI job and maintenance ownership |

## Product improvements

- confirm whether charts must represent all data; add aggregate APIs if needed
- add alert transaction-ID filtering if the product owner confirms the need
- design new-payee cooldown only as a separate, approved requirement with
  persistence/time-boundary tests
- add case assignment, comments, escalation, and maker/checker review only
  after identity and audit design
- define rule configuration ownership, versioning, approval, and change history
- provide controlled demo reset/seed authorization instead of a public mutation

## Security and privacy improvements

- identity provider, least-privilege roles, service identities, and actor audit
- HTTPS, reverse-proxy/network hardening, CSP, rate/payload limits
- managed secrets, rotation, least-privilege DB account, encryption review
- retention, deletion/export, legal hold, masking, data classification
- central logs/metrics/alerts and incident/breach response exercises
- encrypted backup/restore and disaster-recovery tests with agreed RPO/RTO
- independent security, privacy impact, penetration, and legal reviews

## Reliability, scalability, performance, and observability

- establish evidence-based service objectives before adopting thresholds
- profile database queries/indexes and connection-pool behavior under controlled
  load
- test concurrency and alert-state conflicts
- define health/readiness distinctions, graceful shutdown, timeouts, retries,
  resource limits, log rotation, and capacity alarms
- introduce redundancy/failover only when deployment requirements justify it
- store release/image/config identity with operational telemetry

## Accessibility and compatibility

- adopt WCAG 2.2 AA as an engineering target subject to owner/legal review
- extend the implemented axe/Chromium checks to more UI states and browsers
- perform keyboard, focus, zoom/reflow, contrast, screen-reader, and error-state
  manual reviews
- define and verify supported Edge/Chrome/Firefox/mobile widths
- document findings and remediation evidence

## Deployment maturity

Draft PR #46 may be a starting point after its prerequisites are confirmed.
Future deployment should include protected environments, least-privilege
credentials, immutable images, supervised first release, smoke checks,
deployment history, rollback/recovery, backup validation, and private-network
strategy for the actual Linux environment.

## Possible future AI-assisted capability

### User problem

Investigators may need help summarizing linked transaction patterns and
prioritizing review. This capability is intentionally reserved for a governed
future evaluation.

### Proposed use case and value

An optional assistant could generate a clearly labelled, non-authoritative
summary of an alert's already-authorized data and suggest questions for a human
reviewer. It must not close/dismiss alerts, make legal/fraud decisions, or
silently change rules.

### Required data and minimization

Use only fields necessary for the selected alert, with identifiers masked or
pseudonymized where possible. Free-text descriptions/notes require special
redaction and purpose review. Do not train an external model on project data
without explicit contractual/legal approval.

### Governance considerations

- privacy/confidentiality and cross-border processing
- prompt injection through transaction descriptions/notes
- hallucination, omission, inconsistency, and false confidence
- bias in prioritization and unequal review outcomes
- data/model/vendor security and retention
- automation bias and unclear accountability
- latency, availability, and unpredictable cost
- sector/legal restrictions and auditability

### Human oversight and fallback

Every output must cite source fields, show uncertainty, be editable/rejectable,
and require a human decision. If the model is unavailable or fails policy, the
existing deterministic workflow must remain fully usable. No AI output should
become an alert status or evidence without explicit human action and audit.

### Evaluation criteria before implementation

- approved purpose, lawful basis, privacy/security impact assessments, and
  vendor/contract review
- representative synthetic evaluation set and documented ground truth
- factuality/omission/harm/bias metrics with accepted thresholds
- prompt-injection and data-exfiltration red-team tests
- access-control, logging, retention/deletion, and incident tests
- human-factor testing for overreliance and override behavior
- cost/latency/availability limits and kill switch
- monitoring for model/version drift and periodic re-approval

### Conditions required

Identity, least privilege, data governance, legal/privacy approval, threat
model, human-review policy, evaluation results, budget ownership, and a
non-AI fallback must exist before any pilot. A pilot must use synthetic or
approved data and remain clearly separated from implemented production scope.

## Maintenance

Convert a proposal into a tracked requirement/issue only after owner approval.
When implemented and verified, move its factual description to current-state
documents and preserve this proposal/history through linked PRs/ADRs.
