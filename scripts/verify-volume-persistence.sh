#!/usr/bin/env bash
set -euo pipefail

transaction_count() {
    docker compose exec -T database sh -c \
        'MYSQL_PWD="$MYSQL_PASSWORD" mysql --host=127.0.0.1 --batch --skip-column-names --user="$MYSQL_USER" "$MYSQL_DATABASE" --execute="SELECT COUNT(*) FROM transactions"' \
        | tr -d '\r'
}

before_restart="$(transaction_count)"
test "${before_restart}" -ge 1

docker compose down
docker compose up --detach --wait

after_restart="$(transaction_count)"
test "${after_restart}" = "${before_restart}"

printf 'Named-volume persistence: passed (%s rows before and after restart)\n' \
    "${after_restart}"
