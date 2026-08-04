#!/usr/bin/env bash
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

docker_cmd=(docker)
compose_available=false
data_volume="secureflow_mysql-data"

die() {
  echo "ERROR: $*" >&2
  exit 1
}

compose() {
  env \
    -u SECUREFLOW_DB_PASSWORD \
    -u SECUREFLOW_DB_ROOT_PASSWORD \
    -u SECUREFLOW_BIND_ADDRESS \
    -u SECUREFLOW_PORT \
    -u COMPOSE_FILE \
    -u COMPOSE_PROJECT_NAME \
    "${docker_cmd[@]}" compose \
    --project-name secureflow \
    --project-directory "$project_dir" \
    --file "$project_dir/compose.yaml" \
    --env-file "$project_dir/.env" \
    "$@"
}

show_failure_details() {
  local exit_code=$?
  trap - ERR
  if [[ "$compose_available" == true && -f "$project_dir/.env" ]]; then
    echo >&2
    echo "SecureFlow did not start. Container status:" >&2
    compose ps >&2 || true
    echo >&2
    echo "Recent container logs:" >&2
    compose logs --tail=120 app db >&2 || true
  fi
  exit "$exit_code"
}

trap show_failure_details ERR

[[ "$(uname -s)" == "Linux" ]] || die "Run this script inside Linux (Ubuntu/WSL), not PowerShell or Command Prompt."

if ! command -v docker >/dev/null 2>&1; then
  die "Docker is not installed. On Ubuntu, run: bash setup-ubuntu-docker.sh"
fi

if ! docker info >/dev/null 2>&1; then
  if command -v sudo >/dev/null 2>&1 && sudo docker info >/dev/null 2>&1; then
    docker_cmd=(sudo docker)
  else
    if grep -qi microsoft /proc/sys/kernel/osrelease 2>/dev/null; then
      die "The Docker daemon is not reachable. For native Engine run 'sudo systemctl start docker' (or 'sudo service docker start'); if using Docker Desktop, start it and enable Ubuntu WSL integration."
    fi
    die "Docker is installed but not running. Run 'sudo systemctl start docker' and try again."
  fi
fi

if ! "${docker_cmd[@]}" compose version >/dev/null 2>&1; then
  die "Docker Compose is missing. On Ubuntu, run: bash setup-ubuntu-docker.sh"
fi

command -v curl >/dev/null 2>&1 || die "curl is required. On Ubuntu, run: sudo apt-get install -y curl"

compose_available=true

compose_up_help="$("${docker_cmd[@]}" compose up --help 2>&1)"
grep -Eq -- '^[[:space:]]*--wait([[:space:]]|$)' <<< "$compose_up_help" \
  || die "This Docker Compose version is too old; upgrade it so 'docker compose up --wait' is available."
grep -Eq -- '^[[:space:]]*--wait-timeout([[:space:]]|$)' <<< "$compose_up_help" \
  || die "This Docker Compose version is too old; upgrade it so '--wait-timeout' is available."

random_hex() {
  if [[ -r /dev/urandom ]] && command -v od >/dev/null 2>&1 && command -v tr >/dev/null 2>&1; then
    od -An -N32 -tx1 /dev/urandom | tr -d ' \r\n'
  elif command -v openssl >/dev/null 2>&1; then
    openssl rand -hex 32
  else
    die "No secure random-number generator is available. Install coreutils or openssl."
  fi
}

env_value() {
  local key="$1"
  sed -n "s/^${key}=//p" "$project_dir/.env" | tail -n 1 | tr -d '\r'
}

if [[ ! -f .env ]]; then
  if "${docker_cmd[@]}" volume inspect "$data_volume" >/dev/null 2>&1; then
    die "Found existing database volume '$data_volume', but .env is missing. Restore the original .env; generating a new password cannot unlock existing MySQL data. See docs/linux-deployment.md."
  fi

  umask 077
  {
    printf 'SECUREFLOW_DB_PASSWORD=%s\n' "$(random_hex)"
    printf 'SECUREFLOW_DB_ROOT_PASSWORD=%s\n' "$(random_hex)"
    printf 'SECUREFLOW_BIND_ADDRESS=127.0.0.1\n'
    printf 'SECUREFLOW_PORT=8080\n'
  } > .env
  echo "Created private .env credentials for MySQL."
elif [[ -z "$(env_value SECUREFLOW_DB_PASSWORD)" && -n "$(env_value DB_PASSWORD)" ]]; then
  if "${docker_cmd[@]}" volume inspect "$data_volume" >/dev/null 2>&1; then
    die "This database volume uses the earlier .env format. Its original root password cannot be reconstructed safely. Keep the old deployment credentials, or back up and intentionally reset with bash reset-linux.sh."
  fi
  legacy_db_password="$(env_value DB_PASSWORD)"
  legacy_app_port="$(env_value APP_PORT)"
  legacy_app_port="${legacy_app_port:-8080}"
  umask 077
  {
    printf '\n# Migrated automatically from the earlier variable names.\n'
    printf 'SECUREFLOW_DB_PASSWORD=%s\n' "$legacy_db_password"
    printf 'SECUREFLOW_DB_ROOT_PASSWORD=%s\n' "$(random_hex)"
    printf 'SECUREFLOW_BIND_ADDRESS=127.0.0.1\n'
    printf 'SECUREFLOW_PORT=%s\n' "$legacy_app_port"
  } >> .env
  echo "Migrated the existing .env to the current SecureFlow variable names."
fi

chmod 600 .env

db_password="$(env_value SECUREFLOW_DB_PASSWORD)"
db_root_password="$(env_value SECUREFLOW_DB_ROOT_PASSWORD)"
bind_address="$(env_value SECUREFLOW_BIND_ADDRESS)"
app_port="$(env_value SECUREFLOW_PORT)"

[[ "$db_password" =~ ^[A-Za-z0-9._~-]{16,128}$ ]] || die "SECUREFLOW_DB_PASSWORD in .env must be 16-128 letters, numbers, or . _ ~ - characters."
[[ "$db_root_password" =~ ^[A-Za-z0-9._~-]{16,128}$ ]] || die "SECUREFLOW_DB_ROOT_PASSWORD in .env must be 16-128 letters, numbers, or . _ ~ - characters."
[[ "$db_password" != "replace-with-a-strong-password" ]] || die "Replace the example SECUREFLOW_DB_PASSWORD in .env with a strong secret."
[[ "$db_root_password" != "replace-with-a-strong-password" ]] || die "Replace the example SECUREFLOW_DB_ROOT_PASSWORD in .env with a strong secret."
[[ "$bind_address" == "127.0.0.1" || "$bind_address" == "0.0.0.0" ]] || die "SECUREFLOW_BIND_ADDRESS must be 127.0.0.1 (local/WSL) or 0.0.0.0 (controlled remote VM)."
[[ "$app_port" =~ ^[0-9]+$ ]] && (( 10#$app_port >= 1 && 10#$app_port <= 65535 )) || die "SECUREFLOW_PORT must be a number from 1 to 65535."

compose config --quiet

echo "Building and starting SecureFlow..."
compose up --detach --build --remove-orphans --wait --wait-timeout 300

published="$(compose port app 8080)"
published_port="${published##*:}"
[[ "$published_port" =~ ^[0-9]+$ ]] || die "Could not determine the published application port from: $published"
health_url="http://127.0.0.1:${published_port}/actuator/health"
dashboard_url="http://localhost:${published_port}"

health_response="$(curl --fail --silent --show-error --max-time 10 "$health_url")"
[[ "$health_response" == *'"status":"UP"'* ]] || die "Health endpoint did not report UP: $health_response"
curl --fail --silent --show-error --max-time 10 \
  "http://127.0.0.1:${published_port}/api/dashboard/summary" >/dev/null

echo
compose ps
echo
echo "SecureFlow is healthy and its real MySQL database is persistent."
echo "Dashboard:  ${dashboard_url}"
echo "Swagger UI: ${dashboard_url}/swagger-ui.html"
echo "Health:     ${dashboard_url}/actuator/health"

if grep -qi microsoft /proc/sys/kernel/osrelease 2>/dev/null; then
  echo "Open those localhost URLs in your Windows browser."
elif [[ "$bind_address" == "0.0.0.0" ]]; then
  echo "Remote URL:  http://<VM-PUBLIC-IP>:${published_port} (restrict this port in the firewall)"
fi
