#!/usr/bin/env bash
set -Eeuo pipefail

compose_file="${COMPOSE_FILE:-compose.production.yaml}"
wait_seconds="${DEPLOY_WAIT_SECONDS:-180}"

show_failure_details() {
  echo "Deployment failed. Current container status:"
  docker compose -f "$compose_file" ps || true
  docker compose -f "$compose_file" logs --no-color --tail 100 app database || true
}

trap show_failure_details ERR

if [[ ! -f .env ]]; then
  echo "Missing .env in $(pwd). Create it before the first deployment."
  exit 1
fi

echo "Checking the production Compose configuration..."
docker compose -f "$compose_file" config --quiet

echo "Pulling the newest SecureFlow application image..."
docker compose -f "$compose_file" pull app

echo "Starting SecureFlow and waiting for healthy containers..."
docker compose -f "$compose_file" up \
  --detach \
  --remove-orphans \
  --wait \
  --wait-timeout "$wait_seconds"

echo "Checking the application health endpoint..."
docker compose -f "$compose_file" exec -T app \
  wget -q --spider http://localhost:8080/actuator/health

docker compose -f "$compose_file" ps
echo "SecureFlow deployment completed successfully."
