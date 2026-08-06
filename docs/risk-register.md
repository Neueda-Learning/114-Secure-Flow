# Risk register

## Purpose and method

This register is evidence of proactive risk awareness: it connects every
important improvement objective to an existing safeguard and a practical
solution path. Ratings are qualitative engineering priorities rather than
formal enterprise scores; named ownership requires project-owner confirmation.

| ID | Improvement objective | Evidence | Priority if scope expands | Existing safeguard | Solution path |
|---|---|---|---|---|---|
| `R-01` | Add authenticated roles and actor attribution before shared API use. | Controllers/security review | High / Critical | Loopback-only bind | Implement identity, roles, and actor audit before shared exposure |
| `R-02` | Add approved HTTPS and network controls for networked deployment. | Compose/application config | High / High | Loopback bind | Keep local until an approved TLS/reverse-proxy design is verified |
| `R-03` | Promote example credentials to managed, unique secrets. | `.env.example`, Compose, application config | Medium / High | Clear learning-only warning and ignore rules | Apply least privilege, protected storage, and rotation |
| `R-04` | Protect demo seeding with access and resource controls before shared use. | `/api/demo/seed` | High / High | Local-only demo boundary | Disable/protect outside demos and add rate/resource limits |
| `R-05` | Complete container publication with verified immutable identity. | CI run 31084398909; GitHub settings inspection | High / Medium | Quality/build stages pass; package/repository Actions access is Admin | Organization/enterprise Actions owner aligns workflow-token package-write policy; rerun and retain image digest |
| `R-06` | Extend named-volume persistence with tested recovery. | Deployment/security review | Medium / Critical | Named MySQL volume | Define/test encrypted backup, restore, and RPO/RTO |
| `R-07` | Introduce purpose-based retention and linked-record deletion/export for real data. | Schema/UI/API | High / High | Synthetic/local scope and whole-volume reset | Obtain privacy design and automate approved lifecycle rules |
| `R-08` | Maintain database parity as SQL and migrations evolve. | Test config/CI/runtime checks | Medium / High | H2 fast tests plus MySQL 8.4 Compose migration/API/persistence checks | Add focused Testcontainers repository cases only when database-specific complexity grows |
| `R-09` | Automate dependency, container, source, and secret assurance. | Workflow/repository | Medium / High | Manual source review and scoped marker check | Add scanners with triage and patch ownership |
| `R-10` | Make actions and images immutable and repeatable. | Workflow/Docker/Compose/load script | Medium / High | Version/major tags | Pin SHAs/digests and define an update process |
| `R-11` | Make k6 benchmarks fully reproducible. | Load scripts/PR #44/docs | High / Medium | Explicit current-port override documented | Align defaults and retain raw results/environment methodology |
| `R-12` | Add an explicit k6 response-correctness objective. | k6 scripts | Medium / Medium | HTTP duration/failure thresholds | Agree the correctness SLO and add a `checks` threshold |
| `R-13` | Expand browser and accessibility assurance beyond the automated baseline. | Playwright/axe/source review | Medium / Medium | Chromium journey and axe WCAG A/AA scan run in the Compose system job | Add keyboard, screen-reader, zoom/reflow, Firefox/WebKit, and specialist review |
| `R-14` | Strengthen dynamic HTML protection. | `app.js` uses `innerHTML` with helper | Medium / High | Central escaping helper | Add CSP, security tests, and a rendering review checklist |
| `R-15` | Add authenticated actor and tamper-resistant audit evidence. | Entity/schema | High / High | Timestamped status history | Add identity/actor plus externally retained append-only audit controls |
| `R-16` | Evaluate rule accuracy for any future control use. | Monitoring source | High / High | Explainable deterministic rules and human-visible alerts | Define outcomes, tuning, validation, and review/override process |
| `R-17` | Make licensing and redistribution terms explicit. | Repository/technology inventory | High / High | Resolved dependency inventory | Owner/legal selects project license and reviews transitive/container terms |
| `R-18` | Confirm GitHub protection, approvals, merge strategy, and retention. | GitHub history/read limitation | Medium / Medium | PR workflow, CI, templates, contribution guide | Owner verifies/configures settings and records evidence |
| `R-19` | Keep the remote branch list focused and current. | Remote branch inventory | Medium / Low | Branch inventory documented | Review and remove only owner-confirmed obsolete merged branches |
| `R-20` | Preserve clear separation between baseline and proposed work. | PR #46/#47 | Medium / Medium | Draft/open states and documentation | Merge only after requirements, evidence, and review are complete |
| `R-21` | Confirm the chart completeness requirement. | Static JS architecture | Medium / Medium | Simple client-rendered charts | Provide an aggregate API if full-dataset charting is required |
| `R-22` | Add payload, rate, timeout, and query-cost governance for shared use. | Web/config review | Medium / High | Page size capped at 100 | Configure limits and add load/failure tests |
| `R-23` | Complete qualified privacy/legal/security approval before real-data use. | Project scope/compliance review | High / Critical | Explicit synthetic/local educational boundary | Map obligations, controls, owners, and evidence before scope expansion |
| `R-24` | Complete independent human validation of AI-assisted suggestions. | AI usage record | Medium / High | Transparent disclosure and passing tests | Record human review plus security/privacy/license checks |

## Recommended implementation sequence

1. Maintain the controlled local/synthetic boundary.
2. Align GHCR publication and verify artifact identity.
3. Confirm repository protection/review controls and the project license.
4. Add identity, TLS, and managed secrets within an approved deployment design.
5. Add scanning, targeted database edge cases, and backup/restore evidence.
6. Complete qualified privacy/legal review before any real personal-data scope.

## Risk acceptance and closure

Only an identified project/business owner with appropriate security/legal
authority should accept material risk. A risk closes only when implementation
and verification evidence are linked; documentation alone does not mitigate it.

## Maintenance

Review after each material PR, failed control, dependency advisory, deployment,
incident, legal change, or scope change. Preserve closed items with closure
evidence rather than deleting history.
