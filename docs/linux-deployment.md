# SecureFlow Linux deployment

This guide runs the complete SecureFlow product as two Linux containers:

1. `app` — Spring Boot, the dashboard, REST API, Swagger, and Flyway migrations
2. `db` — MySQL 8.4 with a persistent named volume

The database has no published host port. The application is bound to localhost
by default, both containers have health checks and restart policies, and the app
runs as a non-root user with a read-only filesystem.

## Choose the Linux host

Use either:

- **Windows presentation laptop:** Ubuntu under WSL 2, with Docker Engine
  installed inside Ubuntu
- **Linux machine or VM:** Ubuntu 22.04, 24.04, or another release supported by
  Docker Engine

Recommended capacity is 2 GB RAM available to Linux and 10 GB free disk space.
A Windows presentation laptop should have at least 8 GB total RAM. The first
build also needs outbound internet access.

## One-time WSL setup on Windows

This step changes Windows features, requires Administrator permission, and may
require a restart. Do it before presentation day.

1. Open **PowerShell as Administrator**.
2. Install Ubuntu:

   ```powershell
   wsl --install -d Ubuntu
   wsl --update
   ```

3. Restart Windows if requested.
4. Open **Ubuntu** from the Start menu. Wait for installation to finish, then
   choose a Linux username and password. The password will not appear while you
   type; that is normal on Linux.
5. Confirm that the distribution uses WSL 2 from PowerShell:

   ```powershell
   wsl --list --verbose
   ```

   The Ubuntu row should show version `2`.

   If it shows version `1`, convert it and then reopen Ubuntu:

   ```powershell
   wsl --set-version Ubuntu 2
   ```

For best Docker build performance, keep the working copy in Ubuntu's filesystem
under `~/projects`, not permanently under `/mnt/c`.

If the current project is on the Windows desktop, copy it from Ubuntu. Replace
the Windows username/path as needed:

```bash
mkdir -p ~/projects
# Replace YOUR_WINDOWS_USERNAME before running these lines.
windows_source="/mnt/c/Users/YOUR_WINDOWS_USERNAME/Desktop/SecureFlow"
test -d "$windows_source" || { echo "SecureFlow folder not found"; false; }
test ! -e ~/projects/SecureFlow || { echo "~/projects/SecureFlow already exists"; false; }
cp -a "$windows_source" ~/projects/SecureFlow
cd ~/projects/SecureFlow
```

After the final source is committed and pushed, a clean clone is better:

```bash
mkdir -p ~/projects && cd ~/projects
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/Neueda-Learning/114-Secure-Flow.git SecureFlow
cd SecureFlow
```

## One-time Docker Engine setup in Ubuntu

From the SecureFlow directory:

```bash
bash setup-ubuntu-docker.sh
```

The script follows Docker's official Ubuntu repository method. It installs
Docker Engine, Buildx, the Compose plugin, Git, and curl; enables the Docker
service; adds the current Linux user to the `docker` group; and runs the official
`hello-world` verification image. It stops if conflicting container packages are
already installed rather than removing them without permission.

The script can use `sudo docker` immediately. Under WSL, close Ubuntu before
presenting, run this in PowerShell, and reopen Ubuntu so the new group membership
is active:

```powershell
wsl --shutdown
```

On a native Ubuntu machine or VM, log out and reconnect instead. If an older WSL
session is not using systemd, update WSL first; the setup script detects that
case and prints the service-start command.

Verify in the reopened Ubuntu terminal:

```bash
docker --version
docker compose version
docker info --format 'Docker Engine {{.ServerVersion}} on {{.OperatingSystem}}'
```

Membership in the `docker` group grants root-level control of the Linux host, so
only add a trusted presentation account.

## Deploy the product

From the SecureFlow directory:

```bash
bash deploy-linux.sh
```

The script performs all of the following:

- checks that it is running on Linux
- checks Docker Engine and a recent Compose plugin
- creates `.env` with cryptographically random MySQL credentials and mode `600`
- refuses to replace lost credentials for an existing database volume
- validates the Compose configuration
- builds the Java application image
- starts MySQL and waits for an authenticated database query to succeed
- starts the application and waits for Actuator health
- calls both the health and dashboard APIs
- prints the exact dashboard, Swagger, and health URLs
- prints status and recent logs automatically if startup fails

Open in the Windows browser when using WSL:

- <http://localhost:8080>
- <http://localhost:8080/swagger-ui.html>
- <http://localhost:8080/actuator/health>

Expected health output contains `"status":"UP"`. Expected status:

```text
NAME             SERVICE   STATUS
secureflow-app-1   app       Up ... (healthy)
secureflow-db-1    db        Up ... (healthy)
```

Docker may choose longer generated container names, but both services must be
healthy.

## Live presenter runbook

### Rehearse the day before

Do not install WSL, install Docker, pull untested code, or download all images in
front of the presenter. Those steps depend on restarts and network speed.

```bash
cd ~/projects/SecureFlow
bash deploy-linux.sh
docker compose ps
curl -fsS http://localhost:8080/actuator/health
docker compose down
```

`docker compose down` removes the stopped containers and network but preserves
the application image and `secureflow_mysql-data` volume. The live start will be
fast and the rehearsal data will remain. If the presentation must start with an
empty database, run `bash reset-linux.sh` during rehearsal (not live), confirm
the fresh app works, and then run `docker compose down` again.

### Run live

```bash
cd ~/projects/SecureFlow

# Show that this terminal and daemon are Linux
uname -a
docker info --format 'OS={{.OperatingSystem}} Kernel={{.KernelVersion}}'

# Deploy and prove health
bash deploy-linux.sh
docker compose ps
curl -fsS http://localhost:8080/actuator/health

# Prove Flyway created real MySQL tables
docker compose exec -T db sh -c \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SHOW TABLES;"'
```

Now open <http://localhost:8080>, add a transaction, and review the alert. A
short explanation for the presenter is:

> Compose starts a private MySQL container first. Its health check executes an
> authenticated SQL query. Once MySQL is healthy, the Spring Boot container
> starts, Flyway creates or validates the schema, and the deployment waits until
> the HTTP health endpoint reports UP. The browser then uses the real REST API
> and persistent MySQL database.

If asked to prove persistence:

```bash
docker compose down
bash deploy-linux.sh
```

Refresh the dashboard and show that the transaction is still present. Do not add
`--volumes`; that option deletes the database volume.

## Run on a remote Ubuntu VM

The default bind address is safe for WSL/local use and cannot be reached from
another computer. On a controlled VM, change only the bind-address line after
`.env` has been generated; do not overwrite its password lines:

```bash
sed -i 's/^SECUREFLOW_BIND_ADDRESS=.*/SECUREFLOW_BIND_ADDRESS=0.0.0.0/' .env
bash deploy-linux.sh
```

Open `http://VM_PUBLIC_IP:8080`. The address printed by `hostname -I` is often a
private VM address, so use the public address shown by the cloud provider. In the
cloud firewall/security group, allow TCP 8080 only from the presentation network
or your own public IP. Never expose MySQL port 3306. SecureFlow has no built-in
authentication or TLS, so do not leave port 8080 open to the public internet. A
public deployment requires an HTTPS reverse proxy and application authentication.

## Routine operations

```bash
# Container status and health
docker compose ps

# Last 200 log lines from both containers
docker compose logs --tail=200

# Follow application logs; Ctrl+C stops following, not the app
docker compose logs --follow app

# Restart both services
docker compose restart

# Stop while keeping data
docker compose down

# Start/rebuild and verify again
bash deploy-linux.sh
```

Change `SECUREFLOW_PORT` in `.env` if port 8080 is occupied, then rerun the
deployment script. Keep `SECUREFLOW_BIND_ADDRESS=127.0.0.1` on WSL.

### Safe update

Perform and test updates before presentation day:

```bash
git pull --ff-only &&
docker compose pull db &&
docker compose build --pull app &&
bash deploy-linux.sh
```

Create and verify a backup before every update.

## Back up and restore

A Docker volume provides persistence, but it is not an independent backup. To
create a logical SQL backup:

```bash
mkdir -p backups
backup_file="backups/secureflow-$(date +%Y%m%d-%H%M%S).sql"
docker compose exec -T db sh -c \
  'exec mysqldump --single-transaction --no-tablespaces -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  > "$backup_file"
test -s "$backup_file" && echo "Saved $backup_file"
```

The `backups/` directory and `.env` are ignored by Git and the Docker build
context. Store important backups away from the laptop as well.

To restore a selected dump, stop the app so it cannot write during restoration:

```bash
docker compose stop app
docker compose exec -T db sh -c \
  'exec mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < backups/your-selected-backup.sql
bash deploy-linux.sh
```

Test the restore procedure with non-important data before relying on it.

## Reset all data

The following is intentionally destructive. It removes every SecureFlow
transaction, alert, history entry, and database credential stored by Docker.
Create a backup first if any data matters.

```bash
bash reset-linux.sh
```

The reset script explains exactly what will be removed and continues only after
you type `RESET`. It can also reset a stack whose `.env` is already missing; it
never deletes `.env` unless Compose successfully removes the database volume.

## Credential recovery rule

`.env` and `secureflow_mysql-data` are a pair. Never edit either database
password after MySQL has initialized. If `.env` is missing but the volume still
exists, `deploy-linux.sh` deliberately stops.

- Best recovery: restore the original `.env` from a private backup.
- If the data is backed up as SQL: perform the explicit reset above, then restore
  the SQL dump.
- If neither credentials nor a data backup exists: do not delete the volume;
  seek MySQL administrator recovery help first.

## Troubleshooting

### `WSL is not installed`

Run `wsl --install -d Ubuntu` in Administrator PowerShell and restart Windows.

### Docker is installed but not running

```bash
sudo systemctl start docker
sudo systemctl status docker --no-pager
```

If systemd is unavailable, run `sudo service docker start`.

### Permission denied on `/var/run/docker.sock`

The group change has not reached the current terminal. Close Ubuntu, run
`wsl --shutdown` in PowerShell, reopen Ubuntu, and retry. `deploy-linux.sh` can
use `sudo docker` until then.

### Port 8080 is already allocated

Set a free port such as `SECUREFLOW_PORT=8081` in `.env` and rerun the script.
Open the URL printed by the script.

### Existing volume but `.env` is missing

Restore the original `.env`. A newly generated password will not change the
password already stored inside MySQL. See **Credential recovery rule** above.

### A container is unhealthy

```bash
docker compose ps
docker compose logs --tail=200 db app
```

The deployment script also prints these automatically after a failed startup.
The first run can take several minutes while images and Maven dependencies are
downloaded.

### A clean Maven build fails on Windows

Stop any locally running SecureFlow Java process before `clean verify`; Windows
cannot delete a log file while another process holds it open. Docker deployment
does not require a host Maven build.

## Official installation references

- [Install WSL](https://learn.microsoft.com/windows/wsl/install)
- [Install Docker Engine on Ubuntu](https://docs.docker.com/engine/install/ubuntu/)
- [Docker Linux post-installation steps](https://docs.docker.com/engine/install/linux-postinstall/)
- [Docker Compose `up`](https://docs.docker.com/reference/cli/docker/compose/up/)
