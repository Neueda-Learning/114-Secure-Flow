# SecureFlow

SecureFlow is a complete, runnable transaction-monitoring application. It stores
transactions, evaluates three transparent monitoring rules immediately, creates
alerts, and records each alert status change in an audit trail.

The Linux deployment runs the real Spring Boot application and a real,
persistent MySQL database. H2, Mockito, and other test tools are not included in
the production container. The product starts with an empty database: it does not
insert sample transactions, sample alerts, or fixed dates.

## Tomorrow: the easiest setup for every friend

Each person opens **Ubuntu**, then copies these commands one at a time:

```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/Neueda-Learning/114-Secure-Flow.git SecureFlow
cd SecureFlow
bash setup-ubuntu-docker.sh
bash deploy-linux.sh
```

The setup script installs Docker only when it is missing. If it asks for a
password, enter the Ubuntu password; typed password characters are intentionally
invisible. If it asks you to reopen Ubuntu, do that, return to the project with
`cd ~/SecureFlow`, and run `bash deploy-linux.sh` again.

When the script prints **SecureFlow is healthy**, open
[http://localhost:8080](http://localhost:8080). No Java, Maven, MySQL, or Node.js
installation is needed because Docker supplies the production runtime.

## How the live data works (beginner version)

1. You enter a real account, payee, and INR amount in the browser.
2. JavaScript sends that information as JSON to `POST /api/transactions`.
3. Spring Boot adds the server's current time; the browser cannot invent it.
4. Spring Boot saves the transaction in MySQL and runs all three rules.
5. Any matching alerts are saved in the same MySQL database.
6. The dashboard reads the saved rows through the API and shows times in IST.

MySQL stores instants in UTC because that is safe and unambiguous. The screen
converts them to Indian Standard Time, and “Today” means midnight-to-midnight in
`Asia/Kolkata`. Data remains after `docker compose down`; only the intentionally
destructive `bash reset-linux.sh` command deletes it.

## What the product does

- Records and searches INR transactions.
- Uses the server's real current time for every transaction, displays it in IST,
  and calculates dashboard totals using the current Indian calendar day.
- Raises a **HIGH** alert above `₹10,000.00`.
- Raises a **HIGH** alert when an account records more than five transactions in
  ten minutes.
- Raises a **MEDIUM** alert the first time an account uses a payee.
- Supports `OPEN → ACKNOWLEDGED → INVESTIGATING → CLOSED` and dismissal from
  acknowledged or investigating states.
- Shows summary cards, transactions, alerts, rule configuration, linked
  transactions, and alert history in a responsive dashboard.
- Documents the REST API with Swagger UI.

## Run the full product on Linux

SecureFlow has two containers: the application and MySQL. Therefore the correct
beginner command is Docker Compose, wrapped by one setup script, rather than a
single raw `docker run` command.

You need only:

- Ubuntu Linux (a normal Linux machine, VM, or Ubuntu under WSL 2)
- Docker Engine with the Docker Compose plugin
- Internet access for the first image build
- At least 2 GB RAM available to Linux and 10 GB free disk space; use at least
  8 GB total RAM on a Windows presentation laptop

You do **not** need to install Java, Maven, Node.js, or MySQL on the host.

From the project directory in Ubuntu:

```bash
# Only needed once on a fresh Ubuntu installation
bash setup-ubuntu-docker.sh

# Builds, starts, waits for health, and verifies the complete product
bash deploy-linux.sh
```

Then open these URLs. When Ubuntu runs under WSL, open them in the Windows
browser:

- Dashboard: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

The first run can take several minutes because Docker downloads the Java and
MySQL images. Later runs reuse the cache and are much faster.

## Put Ubuntu on a Windows presentation laptop

Open **PowerShell as Administrator** once:

```powershell
wsl --install -d Ubuntu
wsl --update
```

Restart Windows if requested, open **Ubuntu** from the Start menu, and create the
Linux username and password it asks for. Then place the repository in Ubuntu's
Linux filesystem for faster, more reliable Docker builds:

Confirm `wsl --list --verbose` shows Ubuntu at version `2`. If it shows version
`1`, run `wsl --set-version Ubuntu 2` in PowerShell before continuing.

```bash
mkdir -p ~/projects
# Replace YOUR_WINDOWS_USERNAME before running these lines.
windows_source="/mnt/c/Users/YOUR_WINDOWS_USERNAME/Desktop/SecureFlow"
test -d "$windows_source" || { echo "SecureFlow folder not found"; false; }
test ! -e ~/projects/SecureFlow || { echo "~/projects/SecureFlow already exists"; false; }
cp -a "$windows_source" ~/projects/SecureFlow
cd ~/projects/SecureFlow
bash setup-ubuntu-docker.sh
bash deploy-linux.sh
```

If the final project is already pushed to GitHub, clone it directly instead of
copying it:

```bash
mkdir -p ~/projects && cd ~/projects
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/Neueda-Learning/114-Secure-Flow.git SecureFlow
cd SecureFlow
bash setup-ubuntu-docker.sh
bash deploy-linux.sh
```

See [the Linux deployment guide](docs/linux-deployment.md) for the exact WSL
setup, remote-VM option, backup, update, reset, and troubleshooting steps.

## Live presentation sequence

Install Ubuntu and Docker and run the deployment once **before** presentation
day. This removes restart, large download, and weak-network risks. Stop it with
`docker compose down`; that keeps both the image cache and database data.

In front of the presenter:

```bash
cd ~/projects/SecureFlow
uname -a
docker --version
docker compose version
bash deploy-linux.sh
docker compose ps
curl -fsS http://localhost:8080/actuator/health
```

The two services should show `healthy`, and the health response should contain
`"status":"UP"`. To prove that it is a real MySQL deployment:

```bash
docker compose exec -T db sh -c \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SHOW TABLES;"'
```

Open the dashboard, create a transaction, and review its alert. If asked to
prove persistence, run `docker compose down`, run `bash deploy-linux.sh` again,
and show that the transaction is still present.

## Data and credentials

On the first run, `deploy-linux.sh` creates a private `.env` file containing two
random database passwords. MySQL data is stored in the named Docker volume
`secureflow_mysql-data`.

Keep `.env` and that volume together. Do not delete `.env` or change its
passwords while retaining the volume; MySQL initializes its users only when the
volume is first created. The script detects a missing `.env` beside an existing
volume and stops instead of silently locking the app out of its data.

Useful commands:

```bash
# Status
docker compose ps

# Follow application logs
docker compose logs --follow app

# Stop without deleting data
docker compose down

# Start or rebuild again
bash deploy-linux.sh

# Permanent clean reset; asks you to type RESET
bash reset-linux.sh
```

## Security boundary

The default deployment binds to `127.0.0.1`, so only the presentation computer
can reach it. SecureFlow currently has no authentication or TLS termination; do
not expose port 8080 directly to the public internet. A controlled remote VM can
bind to `0.0.0.0` only when its firewall restricts access to the presenter or
trusted network. Public operation also requires authentication and HTTPS in
front of the application.

## Main API

| Method | Route | Purpose |
|---|---|---|
| `POST` | `/api/transactions` | Store and evaluate one transaction |
| `GET` | `/api/transactions` | Search, filter, and paginate transactions |
| `GET` | `/api/alerts` | Filter and paginate alerts |
| `GET` | `/api/alerts/{id}` | Get an alert, linked transactions, and history |
| `PATCH` | `/api/alerts/{id}/status` | Perform one valid status transition |
| `GET` | `/api/rules` | Read the three effective rule configurations |
| `GET` | `/api/dashboard/summary` | Get dashboard totals for the current Indian day |

Request examples are in [docs/api-examples.http](docs/api-examples.http).

## Developer verification

Docker is the recommended way to run the product. Developers who already have
Java 21 can run the same quality gate as CI:

```bash
./mvnw clean verify
```

The suite currently contains 25 tests and enforces at least 70% line coverage
for non-trivial backend code. The JaCoCo report is written to
`target/site/jacoco/index.html`.

More detail:

- [Architecture](docs/architecture.md)
- [Linux/WSL deployment](docs/linux-deployment.md)
- [API examples](docs/api-examples.http)
- [Contributing workflow](CONTRIBUTING.md)
