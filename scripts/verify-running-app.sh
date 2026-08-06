#!/usr/bin/env bash
set -euo pipefail

base_url="${APP_BASE_URL:-http://127.0.0.1:${APP_PORT:-8080}}"
unique_suffix="${GITHUB_RUN_ID:-local}-$(date +%s)"
account_id="SMOKE-${unique_suffix}"
payee_id="PAYEE-${unique_suffix}"

health_response="$(curl --fail --silent --show-error \
    --retry 10 --retry-delay 2 "${base_url}/actuator/health")"
grep -q '"status":"UP"' <<<"${health_response}"

create_response="$(curl --fail --silent --show-error \
    --request POST "${base_url}/api/transactions" \
    --header 'Content-Type: application/json' \
    --data "{\"accountId\":\"${account_id}\",\"payeeId\":\"${payee_id}\",\"amount\":12500.00,\"currency\":\"INR\",\"description\":\"Compose smoke test\"}")"
grep -q '"currency":"INR"' <<<"${create_response}"
grep -q '"generatedAlerts"' <<<"${create_response}"

search_response="$(curl --fail --silent --show-error \
    "${base_url}/api/transactions?search=${account_id}")"
grep -q "\"accountId\":\"${account_id}\"" <<<"${search_response}"

runtime_uid="$(docker compose exec -T app id -u | tr -d '\r')"
test "${runtime_uid}" != "0"

mysql_rows="$(docker compose exec -T database sh -c \
    'MYSQL_PWD="$MYSQL_PASSWORD" mysql --batch --skip-column-names --user="$MYSQL_USER" "$MYSQL_DATABASE" --execute="SELECT COUNT(*) FROM transactions"' \
    | tr -d '\r')"
test "${mysql_rows}" -ge 1

printf 'Health: UP\n'
printf 'MySQL transaction rows: %s\n' "${mysql_rows}"
printf 'Application container UID: %s (non-root)\n' "${runtime_uid}"
printf 'Transaction create/search smoke check: passed\n'
