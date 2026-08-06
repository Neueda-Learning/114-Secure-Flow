# Security policy

## Purpose and scope

This file explains vulnerability reporting and the supported project baseline.
It provides evidence-based security guidance while reserving formal assurance
for qualified review. Implemented controls and the maturity roadmap are in
[Security and threat model](docs/security-and-threat-model.md).

## Supported version

The latest commit on `main` receives fixes. Tagged releases and a
long-term-support policy are available as future governance enhancements.

## Report a vulnerability privately

Do not publish sensitive vulnerability details in a normal public issue. Use
the repository **Security** tab to open a private security advisory if that
feature is available. Otherwise, contact a repository owner through an approved
private channel; a dedicated security email is a future governance enhancement.

Include only what is necessary:

- affected endpoint, component, and version/commit
- minimal reproduction steps
- expected and observed behavior
- potential impact and preconditions
- suggested mitigation, if known

Do not include real credentials, tokens, private keys, personal data,
transaction data, customer information, or confidential infrastructure
details. Redact logs before sharing.

## Current security boundary

The supplied Compose setup is intentionally scoped to local learning and
demonstrations. Verified controls include a loopback-only host port, a non-root
application container user, request validation, constrained alert transitions,
and a named database volume. Identity, authorization, TLS, rate/CSRF controls,
managed secrets, backup automation, security-event monitoring, and automated
scanning are the documented next-stage controls for networked use.

Keep the supplied application in its controlled local environment until those
shared-environment controls are implemented and verified.

## Secrets and incident response

Never commit `.env`, production connection strings, access tokens, private
keys, certificates, or real data. If a secret is committed:

1. Revoke or rotate it immediately.
2. Determine where it was used and review relevant logs.
3. Notify the responsible owner through the approved incident channel.
4. Remove it from current files without rewriting history unless qualified
   repository/security owners approve a separate remediation plan.
5. Record the incident without disclosing the secret.

Removing a value in a later commit does not invalidate earlier exposure.

## Response expectations

The project currently uses an owner-led response: acknowledge reports, assess
severity, agree a private remediation plan, verify the fix, and disclose
responsibly where appropriate. A formal response-time/remediation service level
is a future governance enhancement.
