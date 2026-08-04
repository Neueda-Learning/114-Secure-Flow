#!/usr/bin/env bash
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

docker_cmd=(docker)

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed. Follow docs/linux-deployment.md first." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  if command -v sudo >/dev/null 2>&1 && sudo docker info >/dev/null 2>&1; then
    docker_cmd=(sudo docker)
  else
    echo "Docker is installed but is not running or your user cannot access it." >&2
    exit 1
  fi
fi

if ! "${docker_cmd[@]}" compose version >/dev/null 2>&1; then
  echo "The Docker Compose plugin is required. Follow docs/linux-deployment.md first." >&2
  exit 1
fi

if [[ ! -f .env ]]; then
  if command -v openssl >/dev/null 2>&1; then
    db_password="$(openssl rand -hex 24)"
  else
    db_password="$(date +%s%N | sha256sum | cut -c1-48)"
  fi

  umask 077
  printf 'DB_PASSWORD=%s\nAPP_PORT=8080\n' "$db_password" > .env
  echo "Created .env with a random database password."
fi

echo "Building and starting SecureFlow..."
"${docker_cmd[@]}" compose up --detach --build --remove-orphans --wait

app_port="$(sed -n 's/^APP_PORT=//p' .env | tail -n 1)"
app_port="${app_port:-8080}"

echo
"${docker_cmd[@]}" compose ps
echo
echo "SecureFlow is healthy. Open http://<VM-PUBLIC-IP>:${app_port}"
