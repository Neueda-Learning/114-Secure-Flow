# Security and threat model

## Purpose, audience, and assurance statement

This assessment documents current controls, threats, and a prioritized security
maturity roadmap for owners, developers, operators, and reviewers. It is based
on source and configuration inspection of `main` commit `9379af1`.

The assessment is a strong engineering baseline that can be complemented by
penetration testing, qualified assurance, SAST/DAST, dependency/container/
infrastructure analysis, and full-history secret scanning as scope matures.

## System and data scope

Assets include:

- transaction/account/payee identifiers, amounts, descriptions, and timestamps
- alerts, rule results, investigation notes, and linked transaction history
- MySQL credentials and stored database volume
- application/container/CI configuration and artifacts
- GitHub repository, workflow token permissions, packages, issues, and PRs
- availability and integrity of monitoring and alert-review behavior

Demo data is synthetic by design. Usage guidance keeps real data out of the
learning environment, with stronger technical restrictions planned before any
scope expansion.

## Trust boundaries and entry points

| Boundary/entry point | Trust assumption | Recommended control extension |
|---|---|---|
| Browser/client -> HTTP endpoints | Local trusted operator | Add identity and transport protection if the boundary expands |
| `/api/transactions` | Caller may create stored data/alerts | Unauthorized creation, abuse, resource exhaustion |
| `/api/alerts/{id}/status` | Caller may change investigation state/notes | Tampering and lack of attributable actor |
| `/api/demo/seed` | Caller may add many records | Unauthorized mutation/data growth |
| Search/paging endpoints | Inputs are untrusted | Validation gaps, enumeration, expensive queries |
| Actuator/OpenAPI/Swagger | Operational/developer metadata | Information disclosure and easy mutation discovery |
| Application -> MySQL | Environment credentials trusted | Credential exposure, excessive privilege, data loss |
| Host -> containers/volume | Local host trusted | Host compromise, volume access, weak secret storage |
| GitHub Actions -> Maven/registries/GHCR | External supply chain trusted | Dependency/action/image compromise, token misuse |
| Future SSH deployment | Not in current baseline | Key/host trust, remote command, production secret risk |

## Implemented controls

| Control | Evidence | Current scope / next enhancement |
|---|---|---|
| Loopback-only host bind | `compose.yaml` | Protects default local Compose; add container/network policy if the boundary expands |
| Non-root application user | `Dockerfile` | Limits application-container privilege; runtime UID inspection can add operational evidence |
| Server-side field validation | DTO/controller/service | Covers formats/ranges; identity, rate, and payload controls are planned shared-use layers |
| ORM parameter binding | JPA repository queries | Reduces SQL injection risk in current queries; future native/dynamic queries need review |
| Controlled alert transitions | `AlertService` and tests | Prevents invalid state order; any caller can invoke valid transitions |
| Resolution-note requirement | Service/test | Provides context; authenticated author and immutable retention are planned audit enhancements |
| Flyway + Hibernate validation | Migration/configuration/CI | Improves schema integrity; startup DB user needs migration rights |
| Non-exposed MySQL port | Database remains internal to Compose | Limits host network exposure in default setup |
| `.env`/build/log ignore rules | `.gitignore`, `.dockerignore` | Reduces accidental inclusion; dedicated history/runtime secret controls can complement it |
| Minimal actuator exposure | health and info only | Keeps metadata scope small; add endpoint authorization for shared use |
| Escaping helper for dynamic HTML | `app.js` | Requires continued review of every HTML construction path |
| CI quality gate | Workflow/Maven/JaCoCo | Automates quality checks; add security scans and retain stage-specific delivery evidence |

## Controls in the active maturity roadmap

- Health checks exist; add monitoring/alert ownership for shared operation.
- Container logs exist; add retention, redaction, centralization, and security
  event rules.
- Local defaults avoid public exposure; add firewall/reverse-proxy evidence if
  the network boundary expands.
- CI limits `GITHUB_TOKEN` to contents read/packages write; pin action commit
  SHAs for stronger immutability.
- A documentation-review pattern check found zero obvious credential markers;
  add a specialist secret/history scan for deeper assurance.
- Browser code includes accessibility semantics; add independent security and
  accessibility browser testing.

## Planned shared-environment controls

- Add authentication, authorization, roles, session management, and actor identity.
- Add TLS termination/HSTS and trusted reverse-proxy configuration.
- Add rate limits, quotas, anti-automation, and request-size policy.
- Define a CSRF strategy for browser-triggered mutations.
- Add a secrets manager, rotation schedule, and least-privilege DB-role guidance.
- Record encryption-at-rest controls and any approved field-level encryption.
- Add retention, deletion, legal hold, data export, and anonymization workflows.
- Add tamper-evident, externally retained audit logs.
- Add dependency/container/SAST/DAST/secret/IaC scanning.
- Add SBOM, artifact signing, provenance verification, and immutable release tags.
- Implement and test backup, restore, and disaster recovery.
- Define incident contacts, severity levels, response times, exercises, and breach process.
- Define availability/recovery objectives, redundancy, and database failover as required.

## Threat analysis

| Threat | Example | Existing mitigation | Recommended next control |
|---|---|---|---|
| Spoofing | Anyone acts as an investigator | Controlled local trust boundary | Add identity, strong authentication, roles, and service accounts before shared use |
| Tampering | Unauthorized alert closure or demo seeding | Lifecycle validation only | Add authorization, actor/audit record, CSRF/API controls, immutable external logs |
| Repudiation | User denies a status change | Timestamp/history without actor | Add authenticated subject, request/correlation ID, retained audit logging |
| Information disclosure | Read all transactions/alerts or OpenAPI | Loopback bind in local Compose | Add access control, TLS, minimization, masking, network policy |
| Denial of service | Repeated seeds/transactions/search/load test | Page-size cap; no rate limit | Add quotas/rate limits, resource limits, DB/query metrics and alarms |
| Elevation of privilege | Container/dependency compromise | Non-root app user | Pin images/actions, scan/sign artifacts, harden capabilities/filesystem/network |
| Injection/XSS | Malicious descriptions/notes rendered in HTML | Validation/JPA binding/escaping helper | Add security tests and CSP; review every `innerHTML` path |
| Supply-chain compromise | Malicious dependency/action/base-image update | Version management and CI | Add Dependabot/Renovate, pinned SHAs/digests, SBOM, signature and vulnerability gates |
| Data loss | Volume deletion/host failure/migration error | Named volume only | Define and test backup, restore, migration rollback/recovery, RPO/RTO |
| Detection error | Simple rule creates false positive/negative | Deterministic config and history | Do not treat as authoritative; evaluate thresholds, outcomes, review/override process |

## Authentication and authorization plan

Before shared deployment, define users/roles and permissions for read-only
monitoring, transaction creation, alert review, administration, demo seeding,
and operational endpoints. Require least privilege and record the actor for
every mutation. Exact technology and identity provider are future decisions.

## Secrets management

Current example/default passwords must be replaced outside local learning.
Recommended controls:

1. unique environment credentials with least-privilege DB grants
2. an approved secret store or protected deployment environment
3. no secret values in Compose files, commands, PRs, screenshots, or logs
4. rotation/revocation and ownership procedure
5. CI permissions reduced per job and protected environments for deployment

Draft PR #46 proposes SSH/GitHub Environment secrets and remains a clearly
separated candidate for supervised validation before inclusion in `main`.

## Logging, monitoring, error handling

Spring/container logs and consistent expected-request errors provide the base.
Next controls are:

- add request/correlation IDs and authenticated actors
- define a security/audit event taxonomy
- define log redaction, retention, and access policy
- add central collection, metrics dashboards, alert thresholds, and ownership
- add a project-specific sanitized handler for unexpected exceptions

Avoid logging request bodies, credentials, full identifiers, or investigation
notes without an approved need and controls.

## Backup, recovery, and incident response

A Docker named volume already provides local persistence. Before important use,
extend it with backup encryption/location/access, frequency, retention, restore
steps, RPO/RTO, migration recovery, and a supervised restore exercise.

Use [the root security policy](../SECURITY.md) for private vulnerability
reporting. Formal incident/breach obligations require owner and legal review;
see [India privacy/compliance](privacy-compliance-india.md).

## Secure development maturity plan

Priority order:

1. Keep network exposure local until identity/TLS/secrets are implemented.
2. Resolve GHCR permissions and establish immutable artifact identity.
3. Add dependency, container, secret, and source scanning with triage ownership.
4. Add MySQL and security regression tests.
5. Pin actions/images, generate SBOM/provenance, and define patch cadence.
6. Implement backup/restore and operational monitoring.
7. Conduct independent threat-model, penetration, privacy, and legal reviews.

## Related documents and maintenance

- [Risk register](risk-register.md)
- [Privacy/compliance](privacy-compliance-india.md)
- [Technology inventory](technology-inventory.md)
- [Deployment](deployment.md)
- [Testing](testing.md)

Reassess after any identity, public exposure, external integration, data model,
deployment, monitoring-rule, or AI-feature change.
