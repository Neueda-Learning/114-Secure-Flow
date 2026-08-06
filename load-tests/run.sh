#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

case "${1:-}" in
  gradual) target="gradual-ramp.js" ;;
  spike) target="spike-1000-concurrent.js" ;;
  *)
    echo "Usage: ./run.sh gradual|spike" >&2
    exit 1
    ;;
esac

# self-heal directory permissions so the k6 container's user can read the mount
chmod -R a+rX "$script_dir" 2>/dev/null || true

network_args=(--network host)
base_url="${BASE_URL:-http://127.0.0.1:8081}"
# Docker Desktop (Windows/macOS) does not support --network host; fall back to the host gateway
if ! docker run --rm --network host hello-world >/dev/null 2>&1; then
  network_args=()
  base_url="${BASE_URL:-http://host.docker.internal:8081}"
fi

echo "Target: $base_url"
docker run --rm -i "${network_args[@]}" -v "$script_dir:/scripts" \
  -e BASE_URL="$base_url" \
  grafana/k6 run "/scripts/$target"
