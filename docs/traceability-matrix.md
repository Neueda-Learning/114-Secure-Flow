# Requirements and rubric traceability matrix

## Purpose, baseline, and status definitions

This matrix connects requirements and readable mentor-rubric criteria to
implementation, tests, GitHub work, and evidence. Baseline: `main` commit
`9379af1`, reviewed 2026-08-06.

Statuses: **Verified**, **Partially verified**, **Documented but not verified**,
**Not implemented**, **Not applicable**, and **Requires clarification**.

## Functional and quality traceability

| ID | Requirement | Repository evidence / document | Source | Test | Issue / PR | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|---|---|---|---|
| `FR-01` | Create normalized INR transaction with server time | [Requirements](requirements.md), [API](api.md) | `TransactionController`, `TransactionService` | `createsAndSearchesATransaction` | #10 / #13; current rewrite #36 | EVD-TEST-001, EVD-CI-001 | Verified | Add explicit timestamp assertion |
| `FR-02` | Reject invalid input/ranges/currency | API validation/error model | Request DTO, service, `ApiErrorHandler` | `badRequestsHaveClearErrors` | #10 / #13; #36 | EVD-TEST-001, EVD-CI-001 | Verified for representative cases | Add parameterized boundaries/payload-size cases |
| `FR-03` | Transaction search and pagination | API query table | Controller/repository/service | Creation test covers text/amount | #17 / #29/#36 | EVD-SRC-001, EVD-CI-001 | Partially verified | Test ID/time/page/max-size combinations |
| `FR-04` | High-amount rule | Architecture monitoring section | `MonitoringService`, config | `amountAndVelocityRulesCreateAlerts` | #11 / #18; #26/#27/#36 | EVD-CI-001 | Partially verified | Add exact 10,000 equality and below-boundary tests |
| `FR-05` | Velocity rule | Architecture monitoring section | `MonitoringService`, repository | Sixth transaction asserts velocity | #16 / #27; #36 | EVD-CI-001 | Partially verified | Add time-window edge, concurrency, linking assertions |
| `FR-06` | First-payee rule | Architecture monitoring section | `TransactionService`, `MonitoringService` | Creation/demo tests | #16 / #27; #36 | EVD-CI-001 | Partially verified | Add explicit repeat-pair suppression test |
| `FR-07` | Filter/page alerts | [API](api.md) | Alert controller/repository/service | Severity/status representative filters | #15/#17 / #27/#29/#36 | EVD-CI-001 | Partially verified | Test paging and all enum combinations |
| `FR-08` | Alert detail, links, history | Architecture/database/API | Alert entity/DTO/service | Lifecycle asserts history/linked transaction | #15 / #27/#36 | EVD-CI-001 | Verified | Add multiple linked transaction detail assertion |
| `FR-09` | Controlled alert lifecycle | Alert lifecycle docs | `AlertService.isAllowed` | Lifecycle/dismissal tests | #15 / #27/#36 | EVD-CI-001 | Verified for representative paths | Parameterize complete transition matrix |
| `FR-10` | Resolution notes for terminal states | API/requirements | `AlertService.updateStatus` | Dismissal short/valid note | #15 / #27/#36 | EVD-CI-001 | Partially verified | Test close note and 500/501 boundaries |
| `FR-11` | All-time dashboard aggregates | Overview/API | Dashboard service/repositories | Dashboard test | #17/#22 / #29/#36 | EVD-CI-001 | Partially verified | Test mixed statuses, amounts, zero state |
| `FR-12` | Dashboard UI/charts/pagination/actions | Architecture/browser section | Static HTML/CSS/JS and Playwright | Static assertions plus transaction/chart browser journey and axe scan | #5/#12/#22/#43 / #6/#14/#29/#33/#36/#45 | EVD-SRC-001, EVD-BROWSER-001 | Partially verified | Expand browser coverage to alert lifecycle, filtering, and pagination |
| `FR-13` | Fresh demo batch and startup-if-empty | API/deployment | Demo service/startup/config | Manual seeding twice | #36 | EVD-CI-001 | Partially verified | Directly test startup empty/non-empty paths |
| `FR-14` | Rules, health, Swagger/OpenAPI | API/technology inventory | Controllers/configuration/dependency | Rules tested; static page tested | #23/#17 / #26/#29/#36 | EVD-SRC-001, EVD-CI-001 | Partially verified | Add health and OpenAPI contract assertions |
| `QR-01` | Java 21 executable JAR | README/technology inventory | `pom.xml`, wrapper | All tests precede package | #1 / #2; #34/#36 | EVD-CI-001, EVD-LOCAL-001 | Verified | Add artifact checksum/provenance |
| `QR-02` | 70% JaCoCo gate | [Testing](testing.md) | `pom.xml` | Suite instruments measured code | Historical build PRs | EVD-CI-001 | Verified for run | Retain exact XML/report percentage as release evidence |
| `QR-03` | Flyway schema + Hibernate validate | Architecture/ADR-002 | Migration/configuration | H2 context plus MySQL 8.4 Compose startup | #3/#23 / #26/#27/#36 | EVD-DB-001, EVD-CI-001, EVD-SYSTEM-001 | Verified locally across H2/MySQL | Retain published CI system-test artifact and add migration-upgrade recovery scenarios |
| `QR-04` | Local Compose start/order/persistence | [Deployment](deployment.md) | `compose.yaml`, verification scripts | Disposable WSL runtime, API/DB round-trip, 21-row stop/start continuity | #31/#32/#34/#36 | EVD-CONTAINER-001, EVD-SYSTEM-001 | Verified locally | Reproduce in published CI and add backup/restore evidence |
| `QR-05` | Non-root image and health check | Security/deployment | `Dockerfile`, smoke script | Health `UP`; application UID 100 | #31/#34 | EVD-CONTAINER-001, EVD-CI-001, EVD-SYSTEM-001 | Verified locally | Retain published CI result and inspect target-host runtime controls |
| `QR-06` | PR/main CI and GHCR delivery | README/workflow docs | Pipeline YAML | Maven plus proposed system/browser jobs | Multiple; draft #46 | EVD-CI-001/002/003, EVD-GH-002 | Partially verified | Quality/build/local-system stages are verified; organization policy must allow package write, then retain the digest and validate server automation separately |
| `QR-07` | Evidence and documentation traceability | Documentation index/matrix | Documentation plus targeted CI/test improvements | Link/consistency/Maven/system/browser validation | Proposed review branch | EVD-DOC-001, EVD-SYSTEM-001, EVD-BROWSER-001, EVD-GH-002 | Verified locally | The enforced one-approval rule supplies governance; obtain the actual independent approval before merge |
| `QR-08` | Avoid committed sensitive values/real data | Security/policies | Ignore/example/config files | Scoped marker check passed | N/A | EVD-SRC-001, EVD-DOC-001 | Partially verified | Add dedicated secret/history scanning and replace examples for shared use |
| `QR-09` | Local-only default bind | Deployment/security | `compose.yaml` | None | #34/#36 | EVD-CONTAINER-001 | Verified by source | Runtime/firewall check for any deployment |
| `QR-10` | Manual load tests separate from normal lifecycle | Testing/load guide | `load-tests`, workflow | Manual scripts only | #38 / #44 | EVD-PR-044 | Verified by source | Align requirement wording, port, correctness threshold, retained output |

## Mentor rubric traceability

The supplied photographs make the four rubric themes and readable criteria
traceable to repository evidence. Local identifiers keep the mapping stable.
Mentor/team observation can complement these strong artifacts for behavioral
criteria such as willingness, mindset, adaptability, and speed.

### 1. Technical proficiency and dual-skilling

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-1.1` | Apply relevant technical skills to project tasks | Java/JS/SQL/YAML/Shell, architecture, technology inventory, source | Verified as artifact breadth | Mentor assessment can add individual proficiency depth |
| `RUB-1.2` | Proactive learning/upskilling outside comfort zone | Multiple technologies and explanatory docs | Requires clarification | Cross-stack artifacts support learning; add a short demo/reflection for the behavioral dimension |
| `RUB-1.3` | Depth in core technologies | Architecture, API, Flyway/JaCoCo/Docker/test documentation and implementations | Partially verified | A technical walkthrough can complement the substantial code/document evidence |
| `RUB-1.4` | Seamless switching between tech stacks | Backend, frontend, database, test, container, CI artifacts | Partially verified | A contributor walkthrough can attribute the clearly demonstrated stack range |
| `RUB-1.5` | Growth mindset in technical challenges | Issues/PR iterations and CI corrections | Requires clarification | Iterative artifacts provide context; mentor observation can confirm the behavioral criterion |
| `RUB-1.6` | Adaptability across modern and legacy technologies | Broad modern stack evidenced; legacy integration is outside the current project scope | Not applicable / requires clarification | Mentor confirms whether legacy work was expected for this challenge |
| `RUB-1.7` | Embrace dual-skilling as opportunity | Cross-stack repository and documentation | Requires clarification | A demo or learning reflection can complement the cross-stack evidence |

### 2. Solution design and implementation

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-2.1` | Design, develop, implement, and own end-to-end delivery | UI -> API -> services -> DB -> tests -> Docker/CI | Verified at team artifact level | Commit, PR, and team evidence can attribute individual ownership |
| `RUB-2.2` | Initiative in proposing improvements | Issues #37-#43 and linked PRs | Partially verified | Issue authorship supports initiative; team confirmation can attribute idea ownership |
| `RUB-2.3` | Understand existing systems and end-to-end processes | Architecture, requirements, data flow, risks | Documented but not independently assessed | A mentor walkthrough can demonstrate the documented understanding directly |
| `RUB-2.4` | Code quality, documentation, best practices | Maven/JaCoCo, seven integration tests, two browser checks, MySQL/Compose smoke, ADRs, evidence review | Partially verified | Add static/security analysis and independent review to complement the successful gates |
| `RUB-2.5` | Translate requirements into working deliverables | Requirements/traceability plus source/tests/PRs | Partially verified | Owner confirmation can promote the reconstructed requirements to approved requirements |
| `RUB-2.6` | Better Git discipline: branching, commits, PR descriptions, evidence | 28 PRs, focused teammate PRs, templates, repository workflow | Partially verified | New templates and workflow guidance standardize future PR scope and evidence |
| `RUB-2.7` | Review-first collaboration via PRs and incremental stories/tasks | PR/issue history, CI, and verified `main` protection requiring one approval | Verified at repository-control level | Retain the actual reviewer approval on this PR as contribution evidence |
| `RUB-2.8` | Keep work visible through updates/demos/small commits/increments | Issue/PR/CI history | Partially verified | Add short sprint/demo notes to the already visible delivery history |
| `RUB-2.9` | Faster, higher-quality feedback and scope management | PR CI and issue acceptance criteria | Partially verified | Retain review timestamps or cycle metrics to quantify the existing feedback loop |
| `RUB-2.10` | Treat feedback loops as core delivery process | CI iterations and review workflow docs | Partially verified | Link retrospective/review notes to complement CI correction evidence |
| `RUB-2.11` | Avoid quick fixes/passive execution/large unclear PRs/late rework | Focused workflow, templates, evidence rules, ADR guidance | Requires clarification | Mentor observation can confirm continued use of the preventative controls |

### 3. Automation and modernization adoption

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-3.1` | Contribute to automation/modernization/digital transformation | Maven Wrapper, Flyway, JaCoCo, Docker, CI, demo/load tools | Verified as implemented automation | Add a before/after timing or effort measure to quantify business value |
| `RUB-3.2` | Leverage tools to streamline work/reduce manual effort/improve agility | One-command build/Compose, automated migration/coverage/MySQL/browser/accessibility CI, demo seed | Verified | Align registry permissions and add security scans to extend the broad automation foundation |
| `RUB-3.3` | Curiosity/advocacy for emerging technologies | AI assistance disclosure and proposed AI evaluation | Requires clarification | The AI-assisted review supplies evidence; mentor discussion can confirm advocacy and learning |
| `RUB-3.4` | Integrate automation for measurable impact/business value | CI/test automation and repeatable demo flow | Partially verified | Retain baseline/time/value measures and raw k6 evidence in the next run |
| `RUB-3.5` | Use AI-driven/next-generation automation in unconventional areas | AI-assisted repository review, traceability, and documentation | Partially verified as development assistance | Record independent human review; product AI remains an optional, governed future use case |
| `RUB-3.6` | Reduce acceptance of outdated processes/hesitation to AI/limiting AI to new projects | Modern workflow and transparent AI record | Requires clarification | A mentor reflection can complement the modern artifact evidence |

### 4. Compliance, security, and risk awareness

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-4.1` | Apply compliance, security, and risk practices; secure coding/regulatory awareness | Threat model, privacy/legal matrix, risk register, validation/local bind/non-root | Partially verified | Add planned identity, TLS, scanning, and qualified assurance to the implemented controls |
| `RUB-4.2` | Assess security/risk conduct requirements and policy adherence | Root security policy and secure-development guidance | Documented but not verified | Organizational training and policy records can complement repository guidance |
| `RUB-4.3` | Proactive identification and mitigation of risks | 24-item risk register and prioritized actions | Partially verified | Risk identification is comprehensive; link implementation evidence as roadmap items close |
| `RUB-4.4` | Integrate security/compliance into daily work | PR template, contributing checklist, traceability | Documented; not yet operationally verified | Future PR use and protected checks can demonstrate the controls in operation |
| `RUB-4.5` | Anticipate regulatory changes and adapt solutions | Phased DPDP analysis, primary sources, maintenance triggers | Documented but not verified | Assign qualified legal ownership and schedule recurring review |
| `RUB-4.6` | Avoid reactive/minimal compliance that overlooks risk for speed | Transparent qualifications and prioritized improvement plan | Requires clarification | Longitudinal mentor observation can complement the risk-aware artifacts |

## Rubric coverage summary

- Artifact-level technical breadth, end-to-end implementation, and automation
  have direct repository evidence.
- Code quality, delivery discipline, measurable value, and security integration
  have direct foundations plus clearly prioritized evidence-strengthening steps.
- Mentor observation can add the human dimension for mindset, willingness,
  adaptability, individual ownership, and training/policy adherence.
- The readable photographed criteria are mapped; the mentor can confirm the
  authoritative wording and final rating.

## Maintenance

Every `Verified` row must retain supporting evidence. Downgrade status when
evidence expires or behavior changes. Never upgrade a behavioral rubric item
solely because documentation says the desired behavior.
