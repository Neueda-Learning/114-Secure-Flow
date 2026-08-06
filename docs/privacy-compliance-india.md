# India privacy, security, and compliance considerations

## Document control

| Field | Value |
|---|---|
| Purpose | Map potentially relevant Indian privacy, cyber-security, accessibility, and financial-sector obligations to current repository evidence and responsible next actions |
| Scope | SecureFlow educational transaction-monitoring demonstration at `main` commit `13738e3` |
| Intended audience | Project owners, developers, reviewers, security/privacy teams, and qualified Indian legal or sector specialists |
| Review snapshot | 2026-08-06 |
| Current status | Engineering assessment; not a legal opinion, certification, or compliance declaration |
| Maintenance owner | Project owner, supported by the security owner and qualified legal/privacy reviewer before any real-data or shared deployment |

## Assurance boundary

SecureFlow currently uses synthetic demonstration identifiers and is designed
for local learning and review. The repository does **not** establish that the
application is legally compliant, suitable for a regulated institution, or
approved for real customer data. Applicability depends on the operating entity,
data, purpose, contracts, sector, deployment location, and the law in force at
the relevant time.

The evidence below separates:

- **Implemented**: directly supported by source, configuration, tests, or a
  retained GitHub Actions run.
- **Documented**: a policy or design expectation exists but has not been
  independently assured.
- **Required before scope expansion**: a control or decision needed before
  shared, regulated, or real-personal-data use.
- **Qualified review required**: applicability or sufficiency must be decided
  by an appropriately qualified owner.

## Authoritative Indian sources reviewed

| Source | Relevance to this assessment | Review note |
|---|---|---|
| [Digital Personal Data Protection Act, 2023](https://www.meity.gov.in/static/uploads/2024/02/Digital-Personal-Data-Protection-Act-2023.pdf) | Digital personal-data processing, duties, rights, safeguards, breach and governance concepts | Apply only after confirming the relevant provision's commencement and the project's role/data flow |
| [Digital Personal Data Protection Rules, 2025 and related notifications](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa?pageTitle=Digital-Personal-Data-Protection-Rules-2025686cadad39.pdf) | Operational detail and the official implementation timeline | MeitY source includes the Rules, corrigendum and timeline material |
| [DPDP phased commencement notification, 13 November 2025](https://www.meity.gov.in/static/uploads/2025/11/c56ceae6c383460ca69577428d36828b.pdf) | Establishes different commencement points for different Act provisions | Timeline summarized below; legal owner must recheck later notifications |
| [Information Technology Act, 2000](https://www.indiacode.nic.in/handle/123456789/1999?locale=en) | Cyber-law and security context | Applicability and interaction with later legislation require legal review |
| [IT Reasonable Security Practices and Sensitive Personal Data Rules, 2011](https://wipolex-res.wipo.int/edocs/lexdocs/laws/en/in/in098en.html) and [Government clarification](https://www.pib.gov.in/newsite/erelcontent.aspx?lang=2&reg=48&relid=74990) | Historical/transitional SPDI and reasonable-security-practice context | Linked for applicability analysis; not treated as a current compliance conclusion |
| [CERT-In directions under section 70B](https://www.cert-in.org.in/PDF/CERT-In_Directions_70B_28.04.2022.pdf) | Incident reporting, time synchronization, log and cooperation considerations | Security/legal owners must determine whether and how the directions apply to a future operator |
| [Rights of Persons with Disabilities Act, 2016](https://www.indiacode.nic.in/handle/123456789/12914?locale=en) | Accessibility and equal-access context | Formal applicability and conformance require qualified review |
| [Guidelines for Indian Government Websites and Apps 3.0](https://guidelines.india.gov.in/) | Indian public-sector accessibility/usability reference | Useful design reference; not claimed as mandatory for this educational application |
| [RBI KYC Amendment Directions, 2025](https://www.rbi.org.in/scripts/NotificationUser.aspx/searchnew/searchnew/NotificationUser.aspx?Id=12866) | Illustrates current regulated-entity KYC, monitoring and audit expectations | SecureFlow is not represented as a KYC control or RBI-approved system |

### DPDP commencement snapshot

The 13 November 2025 Gazette notification provides phased commencement:

| Phase | Notification wording | Engineering consequence as of 2026-08-06 |
|---|---|---|
| Publication date | Listed provisions commenced on publication | Legal owner should identify whether any immediately commenced institutional/governance provisions affect the proposed operator |
| One year from publication | Section 6(9) and section 27(1)(d) commence after one year | Track the 13 November 2026 milestone and revalidate the design before that date if real processing is proposed |
| Eighteen months from publication | The notification lists the main processing, notice, consent, rights, obligations and related provisions for the later phase | Track the 13 May 2027 milestone and complete the required operating controls well before any applicable real-data use |

This is an engineering reading of the official notification, not legal advice.
Later notifications, corrigenda, court decisions, regulator guidance, or entity-
specific obligations can change the applicable position.

## Project data and flow inventory

| Data group | Current fields/examples | Repository source | Current classification and boundary |
|---|---|---|---|
| Transaction | transaction ID, account ID, payee ID, amount, currency, description, timestamps | [`V1__create_tables.sql`](../src/main/resources/db/migration/V1__create_tables.sql), [`CreateTransactionRequest.java`](../src/main/java/com/neueda/secureflow/transaction/dto/CreateTransactionRequest.java) | Synthetic demo data only; identifiers could become personal or confidential if real values were entered |
| Alert | rule, severity, status, message, linked transactions, resolution notes, timestamps | [`AlertEntity.java`](../src/main/java/com/neueda/secureflow/alert/AlertEntity.java), [`AlertHistoryEntity.java`](../src/main/java/com/neueda/secureflow/alert/AlertHistoryEntity.java) | Synthetic operational data; free-text notes create an elevated collection/disclosure risk |
| Demo seed | generated accounts, payees, amounts, descriptions and current timestamps | [`DemoDataService.java`](../src/main/java/com/neueda/secureflow/demo/DemoDataService.java) | Reproducible synthetic demonstration data; endpoint is unauthenticated inside the local trust boundary |
| Runtime/CI | application logs, test reports, JAR, browser evidence and container image | [CI workflow](../.github/workflows/pipeline.yml), [testing guide](testing.md) | No real/customer data is intended; repository policy prohibits secrets and sensitive evidence |
| Database storage | MySQL named volume containing the above records | [`compose.yaml`](../compose.yaml) | Local persistence is verified; approved retention, deletion, backup and recovery controls are not yet implemented |

### Data-flow boundary

`Browser -> HTTP API -> service/rule evaluation -> JPA -> MySQL` is the current
application flow. GitHub Actions separately processes source, synthetic tests,
reports and container artifacts. See the [architecture](architecture.md),
[API guide](api.md), and [security threat model](security-and-threat-model.md)
for trust boundaries and entry points.

## Implemented safeguards and verifiable evidence

| Safeguard | Evidence | Verification status | Limitation |
|---|---|---|---|
| Synthetic demonstration boundary | [`DemoDataService.java`](../src/main/java/com/neueda/secureflow/demo/DemoDataService.java), [README](../README.md), [security policy](../SECURITY.md) | Implemented/documented | The API does not technically prevent a user from entering a real identifier |
| Server-side validation and bounded paging | [`CreateTransactionRequest.java`](../src/main/java/com/neueda/secureflow/transaction/dto/CreateTransactionRequest.java), controllers, `badRequestsHaveClearErrors` | Source and integration-test evidence | Validation is not identity, authorization, purpose control, or data-loss prevention |
| Controlled alert lifecycle and resolution notes | [`AlertService.java`](../src/main/java/com/neueda/secureflow/alert/AlertService.java), [`SecureFlowTest.java`](../src/test/java/com/neueda/secureflow/SecureFlowTest.java) | Integration tests passed in the retained main workflow | Status history does not yet record an authenticated actor |
| Output escaping for dynamic UI content | [`app.js`](../src/main/resources/static/app.js) | Source-inspected central helper | Every dynamic HTML path still requires continuing review; CSP/security browser tests are future work |
| Local-only host exposure | [`compose.yaml`](../compose.yaml) | `127.0.0.1` bind verified by source and system workflow | Does not protect a future externally exposed deployment |
| Non-root container and health check | [`Dockerfile`](../Dockerfile), [`verify-running-app.sh`](../scripts/verify-running-app.sh) | UID 100 and health verified in CI | Host, orchestrator, filesystem and network hardening remain environment-specific |
| MySQL persistence and schema control | Flyway migration, Compose, [`verify-volume-persistence.sh`](../scripts/verify-volume-persistence.sh) | MySQL 8.4 startup and restart continuity passed | Named-volume continuity is not backup, restore, retention or deletion evidence |
| Browser/accessibility automation | [`dashboard.spec.js`](../browser-tests/dashboard.spec.js), Playwright and axe-core | Two browser checks passed; no automatically detectable WCAG A/AA violation in the tested state | Manual keyboard, screen-reader, zoom/reflow, other-state and specialist review remain necessary |
| CI quality and artifact delivery | [Main workflow run 31098653366](https://github.com/Neueda-Learning/114-Secure-Flow/actions/runs/31098653366) | Maven, MySQL/Compose/browser checks and GHCR publication succeeded | No SAST, DAST, secret, dependency, container, SBOM/signing or provenance gate yet |
| Vulnerability reporting and secret-response guidance | [Security policy](../SECURITY.md) | Documented process | Dedicated security contact, service levels and exercises are future governance controls |

## India-focused control assessment

| Area | Current position and evidence | Gap / risk | Required next action and owner | Status |
|---|---|---|---|---|
| Purpose, role, notice and consent | Synthetic educational purpose is explicit | No approved Data Fiduciary/processor mapping or real-data notice | Business/legal owner defines entity role, purpose, lawful basis and notices before real data | Required before scope expansion |
| Data inventory and minimization | Stored fields and flows are mapped above; identifier length/content is bounded | Description and resolution-note free text could collect unnecessary or sensitive content | Product/privacy owner defines permitted content, masking, classification and collection warnings | Partially implemented |
| Accuracy and correction | Server timestamps and input validation improve consistency | No authorized correction workflow or downstream correction evidence | Data owner defines correction authority, audit and propagation where applicable | Required before real data |
| Retention, deletion and legal hold | Whole demo environments can be reset; volume continuity is tested | No purpose-based record retention, linked deletion/export, legal hold or backup expiry | Privacy/data owner approves schedules and developers implement/test lifecycle behavior | Not implemented for real data |
| Data Principal requests and grievance | Not represented as implemented because the scope is synthetic | No identity-verified access, correction, erasure, nomination or grievance workflow | Legal/privacy owner confirms applicable rights and accountable contact; team implements/test approved process | Not implemented |
| Children's data | Outside intended scope | No technical age/guardian control | Prohibit through approved use policy or implement verified controls before such processing | Out of scope; review required if changed |
| Security safeguards | Validation, local bind, non-root runtime, schema control, CI and threat model exist | No identity, authorization, TLS, managed secrets, security scanning, centralized monitoring or tested recovery | Security owner delivers prioritized roadmap and independent assessment before shared use | Partially implemented |
| Personal-data breach response | Private vulnerability path and basic logs exist | No approved personal-data incident classification, notification ownership, evidence retention or exercise | Security/legal owners create and test an incident/breach playbook | Documented foundation |
| CERT-In operations | Logs and GitHub evidence provide an operational starting point | Applicability, time synchronization, log retention/location and incident-reporting process are not approved | Security/legal owners map the current directions to the actual operator and deployment | Qualified review required |
| Cross-border processing and vendors | GitHub, Maven/GHCR and build components are inventoried | Runtime, support, logs, backups, subprocessors, contracts, location and transfers are not assessed for real data | Privacy/procurement/security owners complete vendor and transfer assessment | Required before real data |
| Access, sharing and audit | Local-only boundary and status history reduce the initial scope | Any local caller can read/mutate; no authenticated actor or immutable audit record | Implement least-privilege roles, actor attribution, purpose-limited APIs and protected audit retention | Required before shared use |
| RBI/PMLA/financial-sector obligations | Deterministic demo rules are transparent; RBI source is linked | Rules are not validated KYC/AML/STR/sanctions controls and have no regulated-entity approval | Regulated-entity compliance/legal owners perform obligation and control mapping before adoption | Not represented as implemented |
| Accessibility | Semantic UI features and automated axe check exist | Automation covers only detectable rules in one Chromium state | Accessibility owner completes manual WCAG/GIGW/RPwD assessment and remediation evidence | Partially verified |
| Intellectual property/licensing | Direct/resolved technology inventory exists | No approved project licence, transitive/container licence report or redistribution decision | Repository owner selects licence and completes specialist licence review | Documented inventory; approval pending |

## DPDP engineering approval checklist

Before approving any real-personal-data scope, record evidence-backed answers:

1. Which entity is the Data Fiduciary, processor and Data Principal for each
   flow?
2. Which purpose and lawful basis applies to every field, rule result, log and
   evidence artifact?
3. Which statutory phase and current rule applies on the intended go-live date?
4. What notice is delivered, when, in which language/form, and how is proof
   retained?
5. How are withdrawal, access, correction, erasure, nomination and grievance
   requests authenticated, completed and evidenced where applicable?
6. What retention, deletion, legal-hold and backup schedule applies to
   transactions, alerts, notes, history, logs and evidence?
7. Which safeguards, processor contracts, transfer controls, breach procedures
   and accountable contacts are approved?
8. Do Significant Data Fiduciary, sector-specific, CERT-In or other enhanced
   obligations apply?

## Sector-specific boundary

SecureFlow is not presented as KYC, customer due diligence, suspicious-
transaction reporting, sanctions screening, fraud-loss prevention, or an
RBI/PMLA-approved control. A regulated deployment would require an entity-
specific obligation inventory, model/rule validation, governance, maker-checker
controls, record keeping, audit, reporting, operational resilience and formal
approval. Current deterministic alerts are educational examples only.

## Risk treatment priorities

1. Keep use synthetic and local until identity, authorization, TLS and managed
   secrets are implemented and tested.
2. Add technical warnings/controls that prevent real customer data from being
   entered into demo, test, issue, log and CI channels.
3. Define field-level purpose, classification, retention, deletion and access
   ownership.
4. Add authenticated roles, actor-attributed audit history and immutable
   security/audit-event retention.
5. Implement dependency, secret, source, container and dynamic security scans
   with triage ownership and severity policy.
6. Define and exercise incident response, backup/restore and applicable breach
   or CERT-In reporting decisions.
7. Complete qualified privacy, security, accessibility, sector and legal review
   before any real-data/shared deployment.

These priorities are mirrored in the [risk register](risk-register.md) and
[future scope](future-scope.md).

## Rubric and evidence traceability

| Rubric theme | Repository evidence | Honest evaluation boundary |
|---|---|---|
| Apply compliance, security and risk practices | This assessment, [threat model](security-and-threat-model.md), [risk register](risk-register.md), validation, local bind and non-root runtime | Engineering controls are evidenced; legal compliance is not claimed |
| Proactively identify and mitigate risk | Data/control matrices, trust boundaries and prioritized treatments | Implementation evidence is linked separately from future recommendations |
| Integrate security/compliance into daily work | [PR template](../.github/pull_request_template.md), [contribution checks](../CONTRIBUTING.md), CI and traceability matrix | Continued PR use and independent reviews provide longitudinal evidence |
| Anticipate regulatory change | DPDP phased timeline, dated official-source register and maintenance triggers | Qualified owners must revalidate future notifications and applicability |

Stable evidence identifiers are maintained in the
[evidence index](evidence-index.md); requirement and rubric mappings are in the
[traceability matrix](traceability-matrix.md).

## Known limitations and assumptions

- The source review is current only for the stated date and commit.
- The project contains synthetic demo data by intent, but the unauthenticated
  API does not technically enforce that boundary.
- No qualified legal, privacy, penetration, accessibility, sector or licensing
  opinion is included in the repository.
- No production operator, Data Fiduciary, retention schedule, incident contact,
  vendor assessment or deployment jurisdiction has been approved.
- Automated tests prove their stated scenarios only; they do not demonstrate
  legal compliance or complete security.

## Maintenance guidance

Re-review this document when a law or commencement notification changes, and
whenever the project changes data fields, purpose, user group, geography,
vendor, deployment, retention, external integration, identity model or AI
capability. The project owner should record the reviewer, date, affected
sections, source links and resulting decisions without backdating or replacing
historical evidence.
