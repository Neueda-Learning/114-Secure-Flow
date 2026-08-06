#!/usr/bin/env bash
set -Eeuo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
base_dir="${SECUREFLOW_AUTO_DEPLOY_PATH:-/opt/secureflow}"
service_name="secureflow-auto-deploy"

die() {
  echo "ERROR: $*" >&2
  exit 1
}

random_hex() {
  od -An -N32 -tx1 /dev/urandom | tr -d ' \r\n'
}

install_jq() {
  echo "Installing the small jq JSON reader required by the deployment checker..."
  if command -v dnf >/dev/null 2>&1; then
    sudo dnf install -y jq
  elif command -v yum >/dev/null 2>&1; then
    sudo yum install -y jq
  elif command -v apt-get >/dev/null 2>&1; then
    sudo apt-get update
    sudo apt-get install -y jq
  else
    die "Install jq with this Linux distribution's package manager and retry."
  fi
}

[[ "$(uname -s)" == "Linux" ]] || die "Run this on the Linux Docker server."
[[ "$EUID" -ne 0 ]] || die "Run as the normal deployment user, not root."
[[ "$base_dir" =~ ^/[A-Za-z0-9._/-]+$ && "$base_dir" != "/" ]] \
  || die "SECUREFLOW_AUTO_DEPLOY_PATH must be a safe absolute Linux path."
[[ "/$base_dir/" != *"/../"* ]] || die "SECUREFLOW_AUTO_DEPLOY_PATH cannot contain ..."
[[ -f "$project_dir/scripts/auto-deploy-linux.sh" ]] \
  || die "scripts/auto-deploy-linux.sh is missing."

for command_name in sudo systemctl curl tar docker od tr; do
  command -v "$command_name" >/dev/null 2>&1 || die "$command_name is required."
done
docker info >/dev/null 2>&1 \
  || die "Docker is not running or this user cannot access it."
docker compose version >/dev/null 2>&1 || die "Docker Compose is required."
command -v jq >/dev/null 2>&1 || install_jq

deployment_user="$(id -un)"
deployment_group="$(id -gn)"
sudo install -d -o "$deployment_user" -g "$deployment_group" -m 0750 \
  "$base_dir" "$base_dir/automation" "$base_dir/incoming" \
  "$base_dir/releases" "$base_dir/shared"
sudo install -o "$deployment_user" -g "$deployment_group" -m 0755 \
  "$project_dir/scripts/auto-deploy-linux.sh" \
  "$base_dir/automation/auto-deploy-linux.sh"

env_file="$base_dir/shared/.env"
data_volume="secureflow_mysql-data"
if [[ ! -f "$env_file" ]]; then
  if docker volume inspect "$data_volume" >/dev/null 2>&1; then
    die "Found existing volume $data_volume but no saved .env. Restore its original passwords before continuing."
  fi
  umask 077
  {
    printf 'DB_PASSWORD=%s\n' "$(random_hex)"
    printf 'DB_ROOT_PASSWORD=%s\n' "$(random_hex)"
    printf 'APP_PORT=8081\n'
    printf 'DEMO_SEED_ON_STARTUP=false\n'
  } > "$env_file"
  echo "Created private random database credentials at $env_file."
fi
chmod 600 "$env_file"

service_file="/etc/systemd/system/$service_name.service"
timer_file="/etc/systemd/system/$service_name.timer"
sudo tee "$service_file" >/dev/null <<EOF
[Unit]
Description=Deploy the newest tested SecureFlow main revision
After=network-online.target docker.service
Wants=network-online.target
Requires=docker.service

[Service]
Type=oneshot
User=$deployment_user
Group=$deployment_group
Environment=SECUREFLOW_AUTO_DEPLOY_PATH=$base_dir
WorkingDirectory=$base_dir
ExecStart=$base_dir/automation/auto-deploy-linux.sh
TimeoutStartSec=15min
EOF

sudo tee "$timer_file" >/dev/null <<EOF
[Unit]
Description=Check for a tested SecureFlow revision every five minutes

[Timer]
OnBootSec=1min
OnUnitActiveSec=5min
RandomizedDelaySec=30s
Persistent=true
Unit=$service_name.service

[Install]
WantedBy=timers.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now "$service_name.timer"
sudo systemctl start "$service_name.service"

echo
echo "SecureFlow continuous deployment is installed."
echo "Timer: sudo systemctl status $service_name.timer --no-pager"
echo "Logs:  sudo journalctl -u $service_name.service -n 100 --no-pager"
echo "App:   $base_dir/current"
