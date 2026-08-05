# Security policy

## Supported version

The latest commit on **main** is the supported version.

## Reporting a vulnerability

Do not publish sensitive vulnerability details in a normal public issue.

Use the repository's **Security** tab to open a private security advisory. Include:

- affected endpoint or component
- steps to reproduce
- expected and actual behavior
- possible impact
- any suggested mitigation

Do not include real passwords, access tokens, customer data, or financial data.

## Deployment boundary

The included Docker Compose setup is for local learning and demonstrations.

By default:

- the application binds to 127.0.0.1
- the application container runs as a non-root user
- MySQL data stays in a named Docker volume
- no authentication or HTTPS is provided

Before any public or shared deployment, add:

- user authentication and authorization
- HTTPS through a trusted reverse proxy
- managed secrets instead of example passwords
- firewall and network restrictions
- database backup and restore procedures
- log monitoring and dependency update processes

## Secrets

Never commit:

- .env
- database passwords
- GitHub tokens
- private keys or certificates
- production connection strings

If a secret is accidentally committed, revoke or rotate it immediately. Removing
the text from a later commit does not make the exposed secret safe again.
