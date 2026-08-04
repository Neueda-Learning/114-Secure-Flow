# Deploy SecureFlow on a Linux VM

This deployment runs SecureFlow and MySQL in Docker containers. MySQL data is
kept in a named volume, the database is not exposed publicly, unhealthy
containers are detected automatically, and both services restart after a VM
reboot.

## VM requirements

- A 64-bit Linux VM with at least 2 GB RAM and 10 GB free disk space
- Docker Engine with the Docker Compose plugin
- Git and outbound internet access during the first build
- Inbound TCP port `8080` allowed in the cloud firewall/security group

SecureFlow has no login screen. Restrict port `8080` to the presentation network
or your own IP; do not leave it open to the whole internet.

Use Docker's official installation guide for the VM distribution:

- Ubuntu: <https://docs.docker.com/engine/install/ubuntu/>
- Debian: <https://docs.docker.com/engine/install/debian/>
- Other distributions: <https://docs.docker.com/engine/install/>

Verify Docker before deployment:

```bash
docker --version
docker compose version
docker run --rm hello-world
```

## First deployment

```bash
git clone https://github.com/Neueda-Learning/114-Secure-Flow.git
cd 114-Secure-Flow
bash deploy-linux.sh
```

The first deployment downloads the base images and Maven dependencies, so allow
a few minutes. The script creates a private `.env` with a random database
password, builds from source, starts both containers, waits for health checks,
and prints their status.

Open `http://VM_PUBLIC_IP:8080`, then verify from the VM:

```bash
curl --fail http://localhost:8080/actuator/health
docker compose ps
```

The response should contain `{"status":"UP"}` and both services should be
healthy. Do not expose MySQL port `3306`.

## Presentation-day update

```bash
cd 114-Secure-Flow
git pull --ff-only
bash deploy-linux.sh
```

## Operations

```bash
# Follow application logs
docker compose logs --follow app

# Show recent logs from both services
docker compose logs --tail=200

# Restart without deleting data
docker compose restart

# Stop without deleting data
docker compose down

# Start again and wait for health
docker compose up --detach --wait
```

Do not run `docker compose down --volumes` unless you intentionally want to
delete all SecureFlow data.

If port `8080` is occupied, change `APP_PORT=8080` in `.env`, redeploy, and allow
the replacement port in the cloud firewall. Docker-published ports can interact
unexpectedly with host firewall tools such as UFW, so enforce source restrictions
in the cloud provider's firewall/security group.

After the demonstration, run `docker compose down` or remove the inbound port
rule.
