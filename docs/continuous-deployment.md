# Automatic deployment to the private Linux server

## What this implements

The Linux server is reachable only from the provided Windows VM at the private
address `10.9.77.9`. GitHub-hosted runners cannot SSH to that address. The
repository is also public, so attaching a self-hosted runner would expand the
Windows VM's trust boundary unnecessarily.

SecureFlow therefore uses a pull deployment:

```text
push or merge to main
  -> test-and-package passes
  -> MySQL, Compose and browser checks pass
  -> deployment-candidate job succeeds
  -> GHCR publishes :latest and an immutable sha-<40-character-SHA> tag
  -> Linux detects the tested revision within five minutes
  -> Linux tries to pull that exact tested GHCR image
  -> if the private image is unavailable, Linux builds the same SHA from source
  -> Docker Compose starts and health-checks the result
```

This keeps GHCR as the fast primary path without making registry access a
single point of failure. The source fallback is selected only after the exact
image pull fails or the publication job does not succeed.

## GHCR access

Image publication is working, but the organization package is currently
private. The deployment works immediately through its source-build fallback.
For the faster GHCR path, either an organization package administrator can make
the image public, or the deployment user can sign in once with a GitHub token
that has only `read:packages` access:

```bash
read -rsp "GitHub package token: " GHCR_TOKEN; echo
printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
unset GHCR_TOKEN
```

Do not put the token in this repository, `.env`, a command-line argument, or a
systemd unit. Docker stores the login for the Linux deployment user. Skip this
step if the package is made public.

## Confirmed server requirements

The supplied server was checked manually and has:

- Amazon Linux 2023 on x86-64
- Docker Engine 25.0.8
- Docker Compose 2.31.0 with Buildx
- Git, curl and systemd
- an `ec2-user` account with Docker access and passwordless sudo

## One-time installation

These commands are run inside the Linux SSH session after this change has been
reviewed and merged to `main`:

```bash
cd ~
git clone https://github.com/Neueda-Learning/114-Secure-Flow.git SecureFlow
cd SecureFlow
bash install-auto-deploy-linux.sh
```

If `~/SecureFlow` already exists:

```bash
cd ~/SecureFlow
git pull --ff-only
bash install-auto-deploy-linux.sh
```

The installer:

1. checks Docker and Compose access;
2. installs `jq` from the operating-system package repository when missing;
3. creates `/opt/secureflow` owned by the deployment user;
4. creates strong random MySQL passwords with mode `600`;
5. installs and starts a systemd timer;
6. immediately deploys the newest tested `main` revision, preferring GHCR and
   falling back to a local source build.

If the Docker volume `secureflow_mysql-data` already exists while the matching
private `.env` is missing, installation stops. Do not delete the volume or
invent replacement passwords; restore the original `.env` first.

## Verify deployment

```bash
sudo systemctl status secureflow-auto-deploy.timer --no-pager
sudo journalctl -u secureflow-auto-deploy.service -n 100 --no-pager
cd /opt/secureflow/current
docker compose ps
curl --fail http://127.0.0.1:8080/actuator/health
```

The response should contain `"status":"UP"`, and both application and database
containers should be healthy.

## Open the dashboard from Windows

The application remains bound to Linux localhost. From PowerShell on the
provided Windows VM, keep this SSH tunnel running:

```powershell
ssh -L 8080:127.0.0.1:8080 ec2-user@10.9.77.9
```

Then open <http://localhost:8080> in the Windows browser.

## Operations and recovery

```bash
# Trigger a check now
sudo systemctl start secureflow-auto-deploy.service

# See recent deployment output
sudo journalctl -u secureflow-auto-deploy.service -n 200 --no-pager

# See the deployed revision
cat /opt/secureflow/shared/deployed-sha

# See whether the last deployment used ghcr or source-build
cat /opt/secureflow/shared/deployment-mode

# Check containers
cd /opt/secureflow/current && docker compose ps
```

Normal deployments preserve the named MySQL volume. Never add `--volumes` to
the automatic deployment. Each successful source revision remains under
`/opt/secureflow/releases`, and `/opt/secureflow/current` points to the latest
healthy deployment definition and fallback source.
