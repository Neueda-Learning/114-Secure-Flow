# Docker deployment

The supplied deployment is intentionally local and beginner-friendly.

## Requirements

- Docker Engine or Docker Desktop
- Docker Compose v2
- internet access on the first build
- approximately 2 GB of available memory

Java, Maven, and MySQL do not need to be installed on the host.

## Optional local passwords

Copy the example file:

~~~bash
cp .env.example .env
~~~

On Windows PowerShell:

~~~powershell
Copy-Item .env.example .env
~~~

Change both password values before the first database start.

The defaults make a local demonstration easy but must not be used for a public
or shared environment.

## Start

~~~bash
docker compose config
docker compose up --build --wait
~~~

The first command checks the Compose file. The second command:

1. creates the MySQL volume
2. starts MySQL
3. waits for its health check
4. builds the Spring Boot image
5. starts SecureFlow
6. waits for the application health check

Open http://localhost:8080.

## Check status

~~~bash
docker compose ps
curl --fail http://localhost:8080/actuator/health
~~~

The health response should include:

~~~json
{"status":"UP"}
~~~

## Logs

All services:

~~~bash
docker compose logs --follow
~~~

Application only:

~~~bash
docker compose logs --follow app
~~~

Database only:

~~~bash
docker compose logs --follow database
~~~

## Stop and restart

Stop while keeping data:

~~~bash
docker compose down
~~~

Start again:

~~~bash
docker compose up --build --wait
~~~

The **mysql-data** volume preserves transactions, alerts, and history.

## Permanent reset

Warning: this deletes all saved SecureFlow data.

~~~bash
docker compose down --volumes
~~~

The next start creates an empty MySQL database and Flyway creates fresh tables.

## Published image

After a successful push to main, GitHub Actions publishes:

~~~text
ghcr.io/neueda-learning/114-secure-flow:latest
~~~

Pull it with:

~~~bash
docker pull ghcr.io/neueda-learning/114-secure-flow:latest
~~~

The image contains the application only. It still requires a MySQL database and
the DB_URL, DB_USERNAME, and DB_PASSWORD environment variables.

## Security

The default port mapping is:

~~~yaml
127.0.0.1:8080:8080
~~~

This prevents other computers from connecting directly.

Do not change it to a public bind without adding authentication, HTTPS, a
firewall, managed secrets, monitoring, and backups.

## Troubleshooting

### Port 8080 is already used

Stop the other application using port 8080 or change the host side of the port
mapping.

### Database is unhealthy

~~~bash
docker compose logs database
~~~

If a password was changed after MySQL created its volume, restore the original
password or intentionally reset the volume.

### Application is unhealthy

~~~bash
docker compose logs app
~~~

Look for a database connection error or Flyway migration error.

### Rebuild without cached layers

~~~bash
docker compose build --no-cache
docker compose up --wait
~~~
