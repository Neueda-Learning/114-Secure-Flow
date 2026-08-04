#!/usr/bin/env bash
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

die() {
  echo "ERROR: $*" >&2
  exit 1
}

[[ "$(uname -s)" == "Linux" ]] || die "Run this script inside Linux (Ubuntu/WSL)."
command -v docker >/dev/null 2>&1 || die "Docker is not installed."

docker_cmd=(docker)
if ! docker info >/dev/null 2>&1; then
  if command -v sudo >/dev/null 2>&1 && sudo docker info >/dev/null 2>&1; then
    docker_cmd=(sudo docker)
  else
    die "Docker is not running."
  fi
fi

echo "DANGER: this permanently deletes every SecureFlow transaction, alert,"
echo "history entry, MySQL volume, and local database credential."
read -r -p "Type RESET to continue: " confirmation
[[ "$confirmation" == "RESET" ]] || die "Reset cancelled; nothing was deleted."

env \
  SECUREFLOW_DB_PASSWORD=reset-interpolation-only-password \
  SECUREFLOW_DB_ROOT_PASSWORD=reset-root-interpolation-only-password \
  SECUREFLOW_BIND_ADDRESS=127.0.0.1 \
  SECUREFLOW_PORT=8080 \
  "${docker_cmd[@]}" compose \
  --project-name secureflow \
  --project-directory "$project_dir" \
  --file "$project_dir/compose.yaml" \
  --env-file /dev/null \
  down --volumes --remove-orphans

if "${docker_cmd[@]}" volume inspect secureflow_mysql-data >/dev/null 2>&1; then
  die "The database volume still exists, so .env was kept. Inspect it with: docker volume inspect secureflow_mysql-data"
fi

if [[ -f "$project_dir/.env" ]]; then
  rm -- "$project_dir/.env"
fi

echo "SecureFlow data and credentials were removed. Creating a fresh deployment..."
bash "$project_dir/deploy-linux.sh"
