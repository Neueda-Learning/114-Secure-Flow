# Security Policy

## Supported use

SecureFlow is a classroom demonstration application, not a production banking
system. It does not implement authentication or authorization. Do not process
real financial, customer, credential, or personal data with it.

For a VM demonstration, restrict inbound port `8080` to the presentation
network or the presenter's IP. MySQL port `3306` must remain private. Stop the
deployment or remove the firewall rule after the presentation.

## Reporting a problem

Report suspected vulnerabilities privately to the repository maintainers. Do
not include secrets, exploit data, or real personal information in a public
issue. Rotate any credential immediately if it was committed or displayed.

## Known MVP limitations

- No user authentication, roles, or access control
- No application-level TLS termination
- No rate limiting or denial-of-service protection
- No secret manager integration
- No production backup, retention, or disaster-recovery process

These controls must be designed and reviewed before any use beyond the training
demonstration.
