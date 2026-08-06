# Automatic Linux deployment

This guide configures continuous deployment (CD) from GitHub Actions to one
Linux server. After a successful push to `main`, GitHub publishes the Docker
image, connects to the server over SSH, pulls the newest image, restarts the
application, and checks that SecureFlow is healthy.

## Deployment flow

~~~text
Merge to main
    ↓
Maven tests and JaCoCo pass
    ↓
GitHub publishes :latest and :sha-<commit> images
    ↓
GitHub connects to Linux with SSH
    ↓
Linux runs docker compose pull app
    ↓
Containers restart and must become healthy
~~~

Pull requests run the build and test job only. Deployment runs only after a
successful push to `main`.

## Server requirements

The Linux server needs:

- Docker Engine
- Docker Compose v2.20 or newer
- `curl` for manual health checks
- an SSH user that is allowed to run Docker
- outbound HTTPS access to `ghcr.io`
- inbound SSH access from GitHub-hosted runners

Check the installation on the server:

~~~bash
docker version
docker compose version
curl --version
~~~

Membership in the Docker group is effectively administrator-level access. Use
a dedicated deployment user and protect its SSH key.

## 1. Create the server directory

The examples use `/opt/secureflow` and a deployment user named `deploy`:

~~~bash
sudo mkdir -p /opt/secureflow
sudo chown deploy:deploy /opt/secureflow
~~~

The GitHub workflow uploads `compose.production.yaml` and `linux-deploy.sh` to
this directory on every deployment.

## 2. Create the server environment file

Create `/opt/secureflow/.env` on the Linux server:

~~~dotenv
DB_NAME=secureflow
DB_USERNAME=secureflow
DB_PASSWORD=replace-with-a-long-random-password
DB_ROOT_PASSWORD=replace-with-a-different-long-random-password
APP_PORT=8080
IMAGE_TAG=latest
DEMO_SEED_ON_STARTUP=false
~~~

Protect it:

~~~bash
chmod 600 /opt/secureflow/.env
~~~

The real `.env` file stays on the server. It must never be committed to Git.
Changing a MySQL password after the database volume is created does not update
the existing database user automatically.

## 3. Create a dedicated SSH key

Create a key on a trusted computer:

~~~bash
ssh-keygen -t ed25519 -C "secureflow-github-deploy" -f secureflow-deploy-key
~~~

Add `secureflow-deploy-key.pub` to the deployment user's
`~/.ssh/authorized_keys` file on the Linux server. Keep the private key
`secureflow-deploy-key` for the GitHub secret described below.

Test the key before configuring GitHub:

~~~bash
ssh -i secureflow-deploy-key deploy@your-server.example
~~~

## 4. Record the trusted server host key

Read the host key:

~~~bash
ssh-keyscan -H -p 22 your-server.example
~~~

Verify its fingerprint with the server administrator through a separate,
trusted channel. This prevents GitHub Actions from connecting to an
impersonated server.

## 5. Configure the GitHub production environment

In the repository, open **Settings → Environments** and create an environment
named `production`.

Add these environment secrets:

| Secret | Example or purpose |
|---|---|
| `LINUX_HOST` | Server hostname or IPv4 address |
| `LINUX_USER` | Dedicated SSH user, for example `deploy` |
| `LINUX_SSH_PRIVATE_KEY` | Complete private deployment key |
| `LINUX_KNOWN_HOSTS` | Verified output from `ssh-keyscan` |

Add these environment variables:

| Variable | Value |
|---|---|
| `LINUX_DEPLOY_PATH` | `/opt/secureflow` |
| `LINUX_SSH_PORT` | SSH port; omit it to use `22` |

For fully automatic deployment, do not add a required reviewer to the
`production` environment. Add a reviewer only when the team intentionally
wants manual deployment approval.

## 6. Run the first deployment

Merge an approved pull request into `main`. The **CI and CD** workflow will:

1. run all Maven tests and the JaCoCo quality gate
2. publish `ghcr.io/neueda-learning/114-secure-flow:latest`
3. publish an immutable `sha-<commit>` image for traceability
4. securely upload the production Compose file and deployment script
5. temporarily sign the server in to GitHub Container Registry using an isolated credential directory
6. pull the latest application image
7. start MySQL and SecureFlow with Docker Compose
8. wait up to three minutes for healthy containers
9. call the application health endpoint
10. remove the temporary registry credentials

Any failed step makes the GitHub deployment job fail.

## Verify the server

~~~bash
cd /opt/secureflow
docker compose -f compose.production.yaml ps
curl --fail http://127.0.0.1:8080/actuator/health
docker compose -f compose.production.yaml logs --tail 100 app
~~~

The health response should contain:

~~~json
{"status":"UP"}
~~~

## Data safety

MySQL data is stored in the named `mysql-data` Docker volume. Normal
deployments recreate containers but do not delete that volume.

Never add `docker compose down --volumes` to the deployment script. Back up the
database before migrations or other high-risk production changes.

## Troubleshooting

### SSH step fails

- confirm `LINUX_HOST`, `LINUX_USER`, and `LINUX_SSH_PORT`
- confirm the public key is in `authorized_keys`
- confirm `LINUX_KNOWN_HOSTS` matches the current server key
- confirm the firewall allows SSH from GitHub-hosted runners

### Registry login or pull fails

- confirm the package still exists in GitHub Container Registry
- confirm the workflow has `packages: write` or `packages: read` permission
- confirm the server can reach `ghcr.io` over HTTPS

### Compose reports a missing variable

Confirm `/opt/secureflow/.env` exists and contains both database passwords.

### Application does not become healthy

~~~bash
cd /opt/secureflow
docker compose -f compose.production.yaml ps
docker compose -f compose.production.yaml logs --tail 200 app database
~~~

The GitHub job also prints the latest container status and logs when deployment
fails.

## Security boundary

The production Compose file binds the application to `127.0.0.1`, so it is not
directly public. Put a secured reverse proxy in front of it when remote users
need access. Production exposure also requires HTTPS, authentication, firewall
rules, monitoring, backups, and secret rotation.
