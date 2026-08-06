# India privacy, legal, and compliance considerations

## Important notice

This engineering assessment provides a responsible starting point for privacy
and compliance planning. Formal legal conclusions remain with qualified Indian
legal, privacy, security, and sector specialists because applicability depends
on the entity, data, purpose, contracts, sector, location, and commencement date.

## Scope and current context

SecureFlow is an educational transaction-monitoring demonstration with
synthetic demo identifiers. This privacy-preserving usage boundary is explicit;
technical prevention and additional governance are planned before any future
real-data scope.

## Primary official sources reviewed

- [Digital Personal Data Protection Act, 2023](https://www.meity.gov.in/static/uploads/2024/02/Digital-Personal-Data-Protection-Act-2023.pdf)
- [Digital Personal Data Protection Rules, 2025 and related notifications](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa?pageTitle=Digital-Personal-Data-Protection-Rules-2025686cadad39.pdf)
- [DPDP phased commencement notification, 13 November 2025](https://www.meity.gov.in/static/uploads/2025/11/c56ceae6c383460ca69577428d36828b.pdf)
- [Information Technology Act, 2000](https://www.indiacode.nic.in/handle/123456789/1999?locale=en)
- [IT Reasonable Security Practices and Sensitive Personal Data Rules, 2011 (WIPO Lex copy)](https://wipolex-res.wipo.int/edocs/lexdocs/laws/en/in/in098en.html)
- [Government of India clarification on the 2011 Rules](https://www.pib.gov.in/newsite/erelcontent.aspx?lang=2&reg=48&relid=74990)
- [Rights of Persons with Disabilities Act, 2016](https://www.indiacode.nic.in/handle/123456789/2155?locale=en)
- [Guidelines for Indian Government Websites and Apps (GIGW 3.0)](https://guidelines.india.gov.in/)
- [RBI KYC Amendment Directions, 2025](https://www.rbi.org.in/scripts/NotificationUser.aspx/searchnew/searchnew/NotificationUser.aspx?Id=12866)
- [CERT-In directions under section 70B](https://www.cert-in.org.in/PDF/CERT-In_Directions_70B_28.04.2022.pdf)

The DPDP framework has phased commencement dates. As of the review date,
different provisions have different effective dates; owners must confirm which
requirements are in force at the time and for the proposed processing.

## Assessment matrix

| Area | Current implementation/evidence | Growth opportunity / control objective | Recommended next step | Legal review |
|---|---|---|---|---|
| Lawful purpose, notice, consent | Synthetic-only learning scope is documented | Formal purpose/notice becomes necessary if real-data scope is approved | Keep synthetic-only; define controller/fiduciary role, purpose and notice before real data | Required |
| Data inventory/classification | Schema/source clearly identify stored fields | Add formal personal/sensitive/business classifications and mapped flows | Create a field-level inventory, purpose, source, recipient, location, retention, owner | Required |
| Data minimization | Required fields are limited; free-text descriptions/notes exist | Free text can collect excessive/sensitive data | Define allowed content, reduce fields, warnings, masking and review | Required |
| Accuracy | Server timestamps and field validation improve consistency | Add an authorized correction and data-owner verification workflow for real data | Define correction authority, auditability, and downstream propagation | Required if personal data |
| Retention | Named volume behavior is explicit and predictable | Introduce purpose-based retention and defensible deletion before real data | Define retention per field/purpose; automate deletion and legal holds | Required |
| Deletion/erasure | Whole demo environments can be reset reproducibly | Add linked-record subject deletion/export for approved personal-data use | Design subject lookup, dependency-aware deletion/anonymization, evidence, exceptions | Required |
| Individual rights/grievance | Synthetic demo scope avoids representing a rights workflow as implemented | Add a responsible contact and request workflow when applicability is confirmed | Appoint owner/contact and implement verified request workflow where applicable | Required |
| Children’s data | Children’s data is outside the intended scope | Reinforce the boundary with policy or age/guardian controls if scope changes | Prohibit in usage policy or add verified age/guardian controls | Required before such use |
| Security safeguards | Validation, local bind, non-root container, and documented threat model | Extend controls with identity, TLS, managed secrets, scanning, recovery, and monitoring | Execute prioritized security plan and independent assessment | Required |
| Breach response | Private vulnerability guidance establishes a reporting channel | Add personal-data incident decisions, notification ownership, and exercises | Define detection, containment, evidence, authority, notification, drills | Required |
| Cross-border processing | Build-service locations and uncertainty are transparently identified | Map runtime, backups, logs, support, and recipients before real-data deployment | Review restrictions, notices, contracts, and transfer safeguards | Required |
| Processors/vendors | GitHub, Maven, and registry build services are inventoried | Add runtime processor, contract, subprocessor, retention, and transfer assessment | Keep real data out of build/issues/logs; conduct production vendor review | Required |
| Data sharing/disclosure | Local-only default limits the initial trust boundary | Add identity, purpose-limited APIs, logging, masking, and least privilege for sharing | Implement and test the approved access model | Required |
| IT Act/SPDI transition | Relevant current and transitional sources are linked | Convert the research baseline into an owned applicability decision | Counsel determines applicability and transition with DPDP commencement | Required |
| CERT-In incident/log directions | Container logs provide an operational starting point | Add applicable time synchronization, retention, reporting, and controlled access | Security/legal teams determine applicability and implement the policy | Required |
| Sector-specific financial obligations | Educational scope is explicitly distinguished from regulated controls | Map RBI/PMLA/payment obligations only if a regulated entity adopts the system | Regulated-entity counsel/compliance owners approve requirements before use | Required |
| Accessibility | Skip link, labels, ARIA, keyboard-oriented features, focusable table regions, and a passing automated axe WCAG A/AA scan exist | Automation covers only detectable rules in one Chromium state | Add manual keyboard/screen-reader/zoom/reflow and specialist WCAG/GIGW/RPwD review | Required for formal claim |
| Intellectual property/licensing | Direct and resolved dependency versions are inventoried | Make redistribution terms explicit and assess transitive/container licenses | Owner chooses project license and runs license review | Required |

## DPDP-specific engineering questions

Before real personal data, owners should document:

1. Who is the Data Fiduciary, processor, and Data Principal for each flow?
2. Which purpose and legal basis applies to each field and rule result?
3. What notice is provided, in which languages/form, and when?
4. How are consent withdrawal or other lawful-basis changes handled?
5. How can a person access, correct, erase, or complain, subject to lawful
   exceptions?
6. What retention/deletion schedule applies to transactions, alerts, history,
   logs, backups, and evidence?
7. What safeguards, processor contracts, breach procedures, and contact points
   exist?
8. Do Significant Data Fiduciary or other enhanced obligations apply?

These questions form a concrete approval checklist. Complete operational
answers are required only before the project expands into real-personal-data use.

## Sector-specific boundary

The deterministic rules have a clear educational purpose and are not presented
as KYC, AML, suspicious-transaction reporting, sanctions screening, or another
regulated control. If a regulated use is proposed, entity-specific RBI, PMLA,
payment-security, record-keeping, audit, and reporting requirements must first
be mapped and independently approved.

## Privacy-by-design recommendations

- default to synthetic/non-identifying data in demo environments
- block or warn against real/customer data entry
- minimize free text and define data classification
- add role-based access and attributable audit history
- provide purpose/notice/retention metadata and deletion/export workflows
- encrypt and restrict data, logs, backups, and evidence
- test subject-request and deletion behavior, including linked alerts/history
- complete a DPIA/privacy impact assessment before material real-data use

## Maintenance

Review at every change in law, commencement notification, data field, purpose,
user group, geography, vendor, deployment, retention, external integration, or
AI capability. Record counsel decisions separately; do not rewrite this document
to imply legal approval.
