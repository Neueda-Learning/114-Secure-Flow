# Presentation Checklist

## Before presentation day

- [ ] The final pull request is merged and GitHub Actions is green.
- [ ] Every teammate can explain their component and one design trade-off.
- [ ] The five-minute demo has been rehearsed with a timer.
- [ ] A backup recording or screenshots are available.
- [ ] No real financial, personal, or credential data is used.

## When the VM is supplied

- [ ] Confirm a 64-bit Linux VM with at least 2 GB RAM and 10 GB disk.
- [ ] Install/verify Docker Engine, Compose, Git, and outbound connectivity.
- [ ] Allow inbound TCP `8080` only from the presentation network/presenter IP.
- [ ] Clone `main` and run `bash deploy-linux.sh`.
- [ ] Confirm `docker compose ps` reports `db` and `app` as healthy.
- [ ] Confirm `/actuator/health` returns `{"status":"UP"}`.
- [ ] Open dashboard and Swagger using the public VM address.

## Functional evidence

- [ ] Normal transaction is recorded and searchable.
- [ ] First account/payee combination creates a new-payee alert.
- [ ] Amount above ₹10,000 creates a high-severity alert.
- [ ] Sixth transaction within ten minutes creates a velocity alert.
- [ ] Alert can be acknowledged, investigated, and closed with notes.
- [ ] Invalid transitions and missing resolution notes are rejected.
- [ ] Alert history and linked transactions are visible.
- [ ] Dashboard works at desktop and narrow/mobile widths.

## Engineering evidence

- [ ] CI tests, coverage, MySQL smoke test, and Docker build are green.
- [ ] Architecture, user stories, test strategy, and API docs are ready.
- [ ] Git history shows feature branches and reviewed pull requests.
- [ ] MySQL port `3306` is not published.

## After the presentation

- [ ] Run `docker compose down` if the VM is no longer needed.
- [ ] Remove the inbound `8080` firewall rule.
- [ ] Preserve required screenshots/results and remove temporary demo data.
