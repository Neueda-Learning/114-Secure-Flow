# Requirements and rubric traceability matrix

## Purpose, baseline, and status definitions

This matrix connects requirements and readable mentor-rubric criteria to
implementation, tests, GitHub work, and evidence. Baseline: `main` commit
`13738e3`, reviewed 2026-08-06.

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
| `QR-03` | Flyway schema + Hibernate validate | Architecture/ADR-002 | Migration/configuration | H2 context plus MySQL 8.4 Compose startup | #3/#23 / #26/#27/#36/#48 | EVD-DB-001, EVD-SYSTEM-001, EVD-CI-004 | Verified across H2 and MySQL CI | Add migration-upgrade and recovery scenarios |
| `QR-04` | Local Compose start/order/persistence | [Deployment](deployment.md) | `compose.yaml`, verification scripts | WSL and GitHub CI API/DB round-trip and stop/start continuity | #31/#32/#34/#36/#48 | EVD-CONTAINER-001, EVD-SYSTEM-001, EVD-CI-004 | Verified locally and in CI | Add backup/restore evidence and target-host verification |
| `QR-05` | Non-root image and health check | Security/deployment | `Dockerfile`, smoke script | Health `UP`; application UID 100 | #31/#34/#48 | EVD-CONTAINER-001, EVD-SYSTEM-001, EVD-CI-004 | Verified in local and CI runtime | Inspect target-host runtime controls before deployment |
| `QR-06` | PR/main CI and GHCR delivery | README/workflow docs | Pipeline YAML | Maven, MySQL/Compose/browser gates and main-only publication | #48 | EVD-CI-004, EVD-GH-002 | Verified for merge commit `13738e3` | Retain immutable image digest/SHA tag and validate any target deployment separately |
| `QR-07` | Evidence and documentation traceability | Documentation index/matrix | Documentation and targeted CI/test improvements | Link/consistency/Maven/system/browser validation | #48 | EVD-DOC-001/002, EVD-SYSTEM-001, EVD-BROWSER-001, EVD-CI-004 | Verified for repository evidence; review governance partially verified | PR #48 used an administrator bypass while approval was pending; retain independent approval on future protected merges |
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
| `RUB-1.1` | Apply relevant technical skills to project tasks | Java/JS/SQL/YAML/Shell, architecture, technology inventory, supplementary code-level guide (`EVD-LEARN-001`), source | Verified as artifact breadth | Mentor assessment can add individual proficiency depth |
| `RUB-1.2` | Proactive learning/upskilling outside comfort zone | Owner-confirmed supplementary Flyway/MockMvc/JaCoCo/k6 topics plus their implementations and `EVD-LEARN-001` | Partially verified at artifact level | A contributor demo/reflection can verify the individual learning journey and instructor can confirm course coverage |
| `RUB-1.3` | Depth in core technologies | Architecture, API, and source-backed explanations of Flyway/MockMvc/JaCoCo/k6/Docker/test/runtime implementations (`EVD-LEARN-001`) | Partially verified | A technical walkthrough can demonstrate individual depth using the documented execution and failure paths |
| `RUB-1.4` | Seamless switching between tech stacks | Backend, frontend, database, test, container, CI artifacts and cross-stack execution map | Partially verified | A contributor walkthrough can attribute the clearly demonstrated stack range |
| `RUB-1.5` | Growth mindset in technical challenges | Issues/PR iterations and CI corrections | Requires clarification | Iterative artifacts provide context; mentor observation can confirm the behavioral criterion |
| `RUB-1.6` | Adaptability across modern and legacy technologies | Broad modern stack evidenced; legacy integration is outside the current project scope | Not applicable / requires clarification | Mentor confirms whether legacy work was expected for this challenge |
| `RUB-1.7` | Embrace dual-skilling as opportunity | Cross-stack repository, supplementary learning guide, and implementation links (`EVD-LEARN-001`) | Partially verified at artifact level | A demo or learning reflection can confirm the behavioral dimension |

### 2. Solution design and implementation

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-2.1` | Design, develop, implement, and own end-to-end delivery | UI -> API -> services -> DB -> tests -> Docker/CI | Verified at team artifact level | Commit, PR, and team evidence can attribute individual ownership |
| `RUB-2.2` | Initiative in proposing improvements | Kanban items, issues #37-#43, linked PRs, and `EVD-AGILE-001` | Partially verified | Issue/board ownership supports initiative; team confirmation can attribute idea ownership |
| `RUB-2.3` | Understand existing systems and end-to-end processes | Architecture, requirements, data flow, risks | Documented but not independently assessed | A mentor walkthrough can demonstrate the documented understanding directly |
| `RUB-2.4` | Code quality, documentation, best practices | Maven/JaCoCo, seven integration tests, two browser checks, MySQL/Compose smoke, ADRs, evidence review | Partially verified | Add static/security analysis and independent review to complement the successful gates |
| `RUB-2.5` | Translate requirements into working deliverables | Requirements/traceability plus source/tests/PRs | Partially verified | Owner confirmation can promote the reconstructed requirements to approved requirements |
| `RUB-2.6` | Better Git discipline: branching, commits, PR descriptions, evidence | 29 inventoried PR rows plus current PR #48, focused teammate PRs, templates, repository workflow, Kanban issue/PR linkage (`EVD-AGILE-001`) | Partially verified | New templates and workflow guidance standardize future PR scope and evidence |
| `RUB-2.7` | Review-first collaboration via PRs and incremental stories/tasks | Kanban `In review` stage/linked-PR field, PR/issue history, CI, and verified `main` protection requiring one approval | Partially verified | PR #48 passed CI but used an administrator bypass while review was pending; future merges should retain an independent approval |
| `RUB-2.8` | Keep work visible through updates/demos/small commits/increments | Private Kanban board with 18 issue items, six flow states, burn-up/status insights, public issue/PR/CI history (`EVD-AGILE-001`) | Partially verified | Board visibility and incremental movement are verified; dated demo notes can add presentation/communication evidence |
| `RUB-2.9` | Faster, higher-quality feedback and scope management | WIP indicators, Backlog/Priority views, issue acceptance criteria, PR CI, and burn-up (`EVD-AGILE-001`) | Partially verified | Retain review timestamps or cycle-time measures to quantify the existing feedback loop |
| `RUB-2.10` | Treat feedback loops as core delivery process | Kanban review state, seven enabled project workflows, CI iterations, and review workflow docs | Partially verified | Link dated retrospective/review notes when those activities occur |
| `RUB-2.11` | Avoid quick fixes/passive execution/large unclear PRs/late rework | Scoped board items, visible ownership/flow, focused workflow, templates, evidence rules, ADR guidance | Partially verified at artifact level | Mentor observation can confirm continued team behavior around the preventative controls |

### 3. Automation and modernization adoption

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-3.1` | Contribute to automation/modernization/digital transformation | Maven Wrapper, Flyway, JaCoCo, Docker, CI, demo/load tools | Verified as implemented automation | Add a before/after timing or effort measure to quantify business value |
| `RUB-3.2` | Leverage tools to streamline work/reduce manual effort/improve agility | One-command build/Compose, automated migration/coverage/MySQL/browser/accessibility CI, demo seed, successful GHCR publication | Verified | Add security scans and immutable artifact identity to extend the broad automation foundation |
| `RUB-3.3` | Curiosity/advocacy for emerging technologies | AI assistance disclosure and proposed AI evaluation | Requires clarification | The AI-assisted review supplies evidence; mentor discussion can confirm advocacy and learning |
| `RUB-3.4` | Integrate automation for measurable impact/business value | CI/test automation and repeatable demo flow | Partially verified | Retain baseline/time/value measures and raw k6 evidence in the next run |
| `RUB-3.5` | Use AI-driven/next-generation automation in unconventional areas | AI-assisted repository review, traceability, and documentation | Partially verified as development assistance | Record independent human review; product AI remains an optional, governed future use case |
| `RUB-3.6` | Reduce acceptance of outdated processes/hesitation to AI/limiting AI to new projects | Modern workflow and transparent AI record | Requires clarification | A mentor reflection can complement the modern artifact evidence |

### 4. Compliance, security, and risk awareness

| Rubric ID | Readable criterion | Evidence | Status | Evidence-strengthening action |
|---|---|---|---|---|
| `RUB-4.1` | Apply compliance, security, and risk practices; secure coding/regulatory awareness | [India assessment](privacy-compliance-india.md), threat model, risk register, validation/local bind/non-root and CI system evidence (`EVD-COMPLIANCE-001`) | Partially verified | Add planned identity, TLS, scanning, incident/recovery controls and qualified assurance |
| `RUB-4.2` | Assess security/risk conduct requirements and policy adherence | Root security policy and secure-development guidance | Documented but not verified | Organizational training and policy records can complement repository guidance |
| `RUB-4.3` | Proactive identification and mitigation of risks | 24-item risk register and prioritized actions | Partially verified | Risk identification is comprehensive; link implementation evidence as roadmap items close |
| `RUB-4.4` | Integrate security/compliance into daily work | PR template, contribution checklist, CI, evidence index and PR #48 security/privacy review content | Partially verified | Retain independent review and future security-scan evidence to demonstrate sustained use |
| `RUB-4.5` | Anticipate regulatory changes and adapt solutions | Dated phased DPDP timeline, official-source register, maintenance triggers and `EVD-COMPLIANCE-001` | Documented but not legally verified | Assign qualified legal ownership and schedule recurring review before the 2026/2027 milestones |
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
