#!/usr/bin/env bash
set -Eeuo pipefail

die() {
  echo "ERROR: $*" >&2
  exit 1
}

[[ "$(uname -s)" == "Linux" ]] || die "Run this script inside Ubuntu Linux."
[[ -r /etc/os-release ]] || die "Cannot identify this Linux distribution."

# shellcheck disable=SC1091
source /etc/os-release
[[ "${ID:-}" == "ubuntu" ]] || die "This installer supports Ubuntu only. Follow the official Docker Engine guide for ${PRETTY_NAME:-your distribution}."
[[ "$EUID" -ne 0 ]] || die "Run this as your normal Ubuntu user; the script asks for sudo only when needed."
command -v sudo >/dev/null 2>&1 || die "sudo is required."
linux_user="$(id -un)"
is_wsl=false
uses_systemd=false
if grep -qi microsoft /proc/sys/kernel/osrelease 2>/dev/null; then
  is_wsl=true
fi
if command -v systemctl >/dev/null 2>&1 && [[ "$(ps -p 1 -o comm=)" == "systemd" ]]; then
  uses_systemd=true
fi

print_login_refresh() {
  if [[ "$is_wsl" == true && "$uses_systemd" == true ]]; then
    echo "Before presenting, close Ubuntu, run 'wsl --shutdown' in PowerShell, and reopen Ubuntu."
  elif [[ "$is_wsl" == true ]]; then
    echo "This WSL session is not using systemd. Run 'wsl --update' and 'wsl --shutdown' in PowerShell."
    echo "After reopening Ubuntu, run 'sudo service docker start' if Docker is not already running."
  else
    echo "Before presenting, log out and back in to refresh your docker-group membership."
  fi
}

require_compose_features() {
  local compose_help
  compose_help="$(docker compose up --help 2>&1)"
  grep -Eq -- '^[[:space:]]*--wait([[:space:]]|$)' <<< "$compose_help" \
    || die "Docker Compose is too old; upgrade the Compose plugin so '--wait' is available."
  grep -Eq -- '^[[:space:]]*--wait-timeout([[:space:]]|$)' <<< "$compose_help" \
    || die "Docker Compose is too old; upgrade the Compose plugin so '--wait-timeout' is available."
}

missing_host_packages=()
command -v curl >/dev/null 2>&1 || missing_host_packages+=(curl)
command -v git >/dev/null 2>&1 || missing_host_packages+=(git)
if (( ${#missing_host_packages[@]} > 0 )); then
  sudo apt-get update
  sudo apt-get install -y "${missing_host_packages[@]}"
fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  if ! docker info >/dev/null 2>&1 && ! sudo docker info >/dev/null 2>&1; then
    if [[ "$uses_systemd" == true ]] \
        && sudo systemctl list-unit-files docker.service --no-legend 2>/dev/null | grep -q docker.service; then
      sudo systemctl enable --now docker
    elif [[ -x /etc/init.d/docker ]]; then
      sudo service docker start
    fi
  fi
  if ! docker info >/dev/null 2>&1 && ! sudo docker info >/dev/null 2>&1; then
    die "A Docker CLI is installed, but no daemon is reachable. If it came from Docker Desktop, start Docker Desktop and enable Ubuntu WSL integration. For native Docker Engine, remove the incomplete/conflicting CLI installation and rerun this script."
  fi
  if docker info >/dev/null 2>&1 || sudo docker info >/dev/null 2>&1; then
    if ! docker info >/dev/null 2>&1; then
      sudo usermod -aG docker "$linux_user"
      echo "Added $linux_user to the docker group; start a new login session before presenting."
      print_login_refresh
    fi
    echo "Docker Engine and Docker Compose are already installed."
    require_compose_features
    docker --version
    docker compose version || sudo docker compose version
    exit 0
  fi
fi

conflicts=()
for package_name in docker.io docker-compose docker-compose-v2 podman-docker containerd runc; do
  if dpkg-query -W -f='${Status}' "$package_name" 2>/dev/null | grep -q 'install ok installed'; then
    conflicts+=("$package_name")
  fi
done

if (( ${#conflicts[@]} > 0 )); then
  die "Conflicting container packages are installed: ${conflicts[*]}. Review and remove them using Docker's official Ubuntu installation guide before rerunning this script."
fi

echo "Installing Docker Engine and the Compose plugin from Docker's official Ubuntu repository..."
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

ubuntu_codename="${UBUNTU_CODENAME:-${VERSION_CODENAME:-}}"
[[ -n "$ubuntu_codename" ]] || die "Ubuntu codename is missing from /etc/os-release."
architecture="$(dpkg --print-architecture)"

sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${ubuntu_codename}
Components: stable
Architectures: ${architecture}
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin

if command -v systemctl >/dev/null 2>&1 && [[ "$(ps -p 1 -o comm=)" == "systemd" ]]; then
  sudo systemctl enable --now docker
else
  sudo service docker start
fi

sudo usermod -aG docker "$linux_user"
sudo docker run --rm hello-world >/dev/null
sudo docker compose version
require_compose_features

echo
echo "Docker is installed and verified."
echo "You can run 'bash deploy-linux.sh' now; it will use sudo during this first session."
print_login_refresh
echo "In the new login session, Docker commands will work without sudo."
