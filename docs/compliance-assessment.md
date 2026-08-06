# India Regulatory Compliance Assessment — SecureFlow

**Status:** Local working document, not yet committed/pushed.
**Scope:** Reviewed against RBI, PMLA, DPDP Act 2023, and IT Act 2000
frameworks, in the context of this being a training/classroom project
(per the project brief and `SECURITY.md`), not a production financial
system.
**Date of assessment:** 2026-08-06

---

## 1. Project Context (read this first)

The project brief states this is a training exercise using synthetic/test
transaction data (Appendix H: "Sample Test Data Generator"), with no
requirement to process real financial, customer, or personal data. This
is reinforced by the repo's own `SECURITY.md`:

> "SecureFlow is a classroom demonstration application, not a production
> banking system. It does not implement authentication or authorization.
> Do not process real financial, customer, credential, or personal data
> with it."

This framing is what currently keeps the project outside direct
regulatory scope. The findings below distinguish between "not applicable
today because this is a training exercise" and "would become required if
this were ever used for real money or real customer data."

---

## 2. Findings by Framework

### 2.1 RBI — Storage of Payment System Data (2018 circular)
**Status: Not applicable today; noted for future scope**

- Rule: all payment system data (end-to-end transaction details) must be
  stored only in systems located in India.
- Current state: data is stored in MySQL on an EC2 instance; the specific
  AWS region was not verified during this assessment.
- Applicability: this rule targets licensed "system providers" operating
  real payment systems (banks, PSPs, PPI issuers). This project is not
  one, so the rule does not legally apply today.
- **Future scope (later sprint):** If this project or a derivative were
  ever used with real payment data, verify/require an India-region (e.g.,
  `ap-south-1`) deployment before go-live.

### 2.2 PMLA + RBI KYC/AML Master Direction
**Status: Not applicable today; noted for future scope**

Real regulated entities (banks, NBFCs, payment providers) must, under
this framework:
- Perform Customer Due Diligence (CDD)/KYC before onboarding.
- Monitor transactions for suspicious patterns.
- File Suspicious Transaction Reports (STR) / Cash Transaction Reports
  (CTR) electronically with FIU-IND.
- Appoint a Designated Director and Principal Officer.
- Retain records for at least 5 years.

Current state in this project:
- No KYC/CDD exists — `accountId`/`payeeId` are freeform validated text
  labels (e.g., `ACC-1001`), not verified real-world identities.
- The three monitoring rules (Amount Threshold, Velocity, New Payee) are
  conceptually similar in spirit to AML transaction monitoring, which is
  a nice alignment point, but there is no STR/CTR filing integration with
  FIU-IND, and no Designated Director/Principal Officer role.
- Alerts stay entirely internal to the app; nothing is reported
  externally.

**Applicability:** PMLA/KYC obligations apply specifically to "Reporting
Entities" — actual licensed banks/NBFCs/payment system providers. This
project is not one, so these obligations do not legally bind it today.

**Future scope (later sprint):** This is the single biggest gap if this
project were ever extended to move real money. Would require: real
KYC/CDD onboarding flow, STR/CTR reporting integration, and designated
compliance-officer roles — a substantial scope increase, only relevant
outside the training context.

### 2.3 Digital Personal Data Protection Act, 2023 (DPDP Act)
**Status: Minimal applicability today; noted for future scope**

Key obligations under this Act: consent-based lawful processing of
personal data, security safeguards, breach notification to the Data
Protection Board, and data principal rights (access, correction,
erasure).

Current state in this project:
- `accountId`/`payeeId` are opaque labels, not obviously "personal data"
  of an identifiable individual as currently used in the demo/test data.
- No consent capture flow exists.
- No documented data retention/erasure policy.
- No breach-notification process.

**Applicability:** These gaps are irrelevant today since no real personal
data exists in the system. They would become directly relevant the
moment real names, phone numbers, or identity documents were ever stored.

**Future scope (later sprint):** Before any real personal data is ever
introduced: add a consent flow, a data retention/erasure policy, and a
breach-notification runbook.

### 2.4 IT Act, 2000 — Section 43A (reasonable security practices)
**Status: Gap identified for future scope**

- Requires "reasonable security practices" for sensitive personal data.
- Current state: no authentication, no TLS termination in the app itself,
  no documented encryption-at-rest approach. This is already honestly
  disclosed in the repo's own `SECURITY.md`.
- **Applicability:** Not a compliance failure today because there is no
  sensitive personal data in the system by design (training/demo data
  only). Would become a direct compliance risk if real sensitive personal
  data were ever added without first addressing these gaps.
- **Future scope (later sprint):** Add authentication + TLS before any
  real data is introduced, per the same items already listed in the
  OWASP assessment (A02, A07).

---

## 3. Summary Table

| Framework | Applicable today? | Why | Future scope if real data/usage introduced |
|---|---|---|---|
| RBI Payment Data Storage (2018) | No | Not a licensed payment system provider | Verify India-region data storage |
| PMLA / RBI KYC-AML Master Direction | No | Not a Reporting Entity; no real onboarding | Add KYC/CDD, STR/CTR reporting, compliance officer roles |
| DPDP Act 2023 | Minimal | No real personal data used | Add consent, retention limits, breach-notification process |
| IT Act 2000 §43A | Conditionally (not yet) | No sensitive personal data yet | Add auth/TLS/encryption before real data is added |

---

## 4. Recommendation

Keep this project explicitly framed as a non-production training
exercise (as `SECURITY.md` already does) — that framing is exactly what
keeps it outside direct regulatory scope today. If there is ever a
concrete plan to move this toward real transactions or real user data,
the priority order to become compliant would be:

1. Add authentication + TLS (also required by OWASP A02/A07).
2. Confirm/require India-region data storage.
3. Add a real KYC/CDD flow if the project ever handles real accounts.
4. Build STR/CTR reporting integration only if operating as, or on behalf
   of, a licensed financial entity.
5. Add DPDP-compliant consent capture and data-retention/erasure
   policies.

This document, together with `docs/owasp-assessment.md`, is intended as
reference material for presentation Q&A and for a possible later sprint
if the project scope ever expands beyond the current training exercise.
