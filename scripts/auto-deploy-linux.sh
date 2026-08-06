#!/usr/bin/env bash
set -Eeuo pipefail

repository="Neueda-Learning/114-Secure-Flow"
workflow="pipeline.yml"
image_repository="ghcr.io/neueda-learning/114-secure-flow"
base_dir="${SECUREFLOW_AUTO_DEPLOY_PATH:-/opt/secureflow}"
lock_dir="$base_dir/.deploy-lock"
archive=""

die() {
  echo "ERROR: $*" >&2
  exit 1
}

cleanup() {
  [[ -z "$archive" ]] || rm -f -- "$archive"
  rmdir "$lock_dir" 2>/dev/null || true
}

show_failure_details() {
  local exit_code=$?
  trap - ERR
  echo "Deployment failed. Current SecureFlow status:" >&2
  if [[ -n "${release_dir:-}" && -f "${release_dir:-}/compose.yaml" ]]; then
    compose ps >&2 || true
    compose logs --no-color --tail=120 app database >&2 || true
  fi
  exit "$exit_code"
}

compose() {
  docker compose \
    --project-name secureflow \
    --project-directory "$release_dir" \
    --file "$release_dir/compose.yaml" \
    --env-file "$release_dir/.env" \
    "$@"
}

trap show_failure_details ERR

[[ "$(uname -s)" == "Linux" ]] || die "Run this on the Linux Docker server."
[[ "$base_dir" =~ ^/[A-Za-z0-9._/-]+$ && "$base_dir" != "/" ]] \
  || die "SECUREFLOW_AUTO_DEPLOY_PATH must be a safe absolute Linux path."
[[ "/$base_dir/" != *"/../"* ]] || die "SECUREFLOW_AUTO_DEPLOY_PATH cannot contain ..."

for command_name in curl jq tar docker sed tail tr date; do
  command -v "$command_name" >/dev/null 2>&1 || die "$command_name is required."
done
docker info >/dev/null 2>&1 || die "Docker is not running or this user cannot access it."
docker compose version >/dev/null 2>&1 || die "Docker Compose is required."

mkdir -p "$base_dir/incoming" "$base_dir/releases" "$base_dir/shared"
if ! mkdir "$lock_dir" 2>/dev/null; then
  echo "Another SecureFlow deployment is already running; skipping this check."
  exit 0
fi
trap cleanup EXIT
trap show_failure_details ERR

runs_url="https://api.github.com/repos/${repository}/actions/workflows/${workflow}/runs?branch=main&event=push&per_page=1"
echo "Checking GitHub for the newest tested main revision..."
run_json="$(curl \
  --fail --silent --show-error --location \
  --connect-timeout 10 --max-time 30 \
  --header 'Accept: application/vnd.github+json' \
  --header 'User-Agent: secureflow-auto-deployer' \
  "$runs_url")"

run_id="$(jq -er '.workflow_runs[0].id' <<< "$run_json")"
release_sha="$(jq -er '.workflow_runs[0].head_sha' <<< "$run_json")"
run_status="$(jq -er '.workflow_runs[0].status' <<< "$run_json")"
[[ "$run_id" =~ ^[0-9]+$ ]] || die "GitHub returned an invalid workflow run ID."
[[ "$release_sha" =~ ^[0-9a-f]{40}$ ]] || die "GitHub returned an invalid revision."

jobs_url="https://api.github.com/repos/${repository}/actions/runs/${run_id}/jobs?filter=latest&per_page=100"
jobs_json="$(curl \
  --fail --silent --show-error --location \
  --connect-timeout 10 --max-time 30 \
  --header 'Accept: application/vnd.github+json' \
  --header 'User-Agent: secureflow-auto-deployer' \
  "$jobs_url")"
jq -e '.jobs[] | select(.name == "deployment-candidate" and .conclusion == "success")' \
  >/dev/null <<< "$jobs_json" \
  || { echo "Latest main revision has not passed every required test; nothing will be deployed."; exit 0; }

if [[ "$run_status" != "completed" ]]; then
  echo "The tested revision is still publishing its image; the next timer check will retry."
  exit 0
fi

image_was_published=false
if jq -e '.jobs[] | select(.name == "Publish container image" and .conclusion == "success")' \
  >/dev/null <<< "$jobs_json"; then
  image_was_published=true
fi

state_file="$base_dir/shared/deployed-sha"
if [[ -f "$state_file" && "$(tr -d '\r\n' < "$state_file")" == "$release_sha" ]]; then
  echo "SecureFlow is already at tested revision $release_sha."
  exit 0
fi

release_id="${release_sha}-$(date -u +%Y%m%dT%H%M%SZ)"
release_dir="$base_dir/releases/$release_id"
archive="$base_dir/incoming/$release_id.tar.gz"
source_url="https://github.com/${repository}/archive/${release_sha}.tar.gz"

[[ ! -e "$release_dir" ]] || die "Release directory already exists: $release_dir"
echo "Downloading tested revision $release_sha..."
curl \
  --fail --silent --show-error --location \
  --connect-timeout 10 --max-time 120 \
  --output "$archive" \
  "$source_url"

mkdir -p "$release_dir"
tar --extract --gzip --file "$archive" --directory "$release_dir" --strip-components=1
ln -s "$base_dir/shared/.env" "$release_dir/.env"

app_port="$(sed -n 's/^APP_PORT=//p' "$base_dir/shared/.env" | tail -n 1 | tr -d '\r')"
app_port="${app_port:-8081}"
[[ "$app_port" =~ ^[0-9]+$ ]] && (( 10#$app_port >= 1 && 10#$app_port <= 65535 )) \
  || die "APP_PORT in the private .env must be between 1 and 65535."

compose config --quiet
deployment_mode="source-build"
image_ref="$image_repository:sha-$release_sha"
if [[ "$image_was_published" == "true" ]]; then
  echo "Trying immutable tested image $image_ref..."
  if docker pull "$image_ref"; then
    export APP_IMAGE="$image_ref"
    deployment_mode="ghcr"
    echo "Starting the tested GHCR image..."
    compose up --no-build --pull missing --detach --remove-orphans --wait --wait-timeout 300
  else
    echo "GHCR pull was unavailable; building the same tested revision locally instead." >&2
  fi
else
  echo "The image publication job did not succeed; using the tested source fallback." >&2
fi

if [[ "$deployment_mode" == "source-build" ]]; then
  unset APP_IMAGE
  echo "Building and starting tested revision $release_sha..."
  compose up --build --detach --remove-orphans --wait --wait-timeout 300
fi

health_response="$(curl \
  --fail --silent --show-error \
  --connect-timeout 5 --max-time 15 \
  "http://127.0.0.1:${app_port}/actuator/health")"
[[ "$health_response" == *'"status":"UP"'* ]] \
  || die "Health endpoint did not report UP: $health_response"

ln -sfn "$release_dir" "$base_dir/current"
umask 077
printf '%s\n' "$release_sha" > "$state_file"
printf '%s\n' "$deployment_mode" > "$base_dir/shared/deployment-mode"
compose ps
echo "Continuous deployment completed successfully for $release_sha using $deployment_mode."
