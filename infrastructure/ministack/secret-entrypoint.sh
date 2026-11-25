#!/bin/sh
set -eu
i=0
until secret=$(aws --cli-connect-timeout 3 --cli-read-timeout 5 --endpoint-url "$SECRETS_ENDPOINT" secretsmanager get-secret-value --secret-id "$DB_SECRET_NAME" --query SecretString --output text); do
  i=$((i + 1))
  if [ "$i" -ge 15 ]; then
    echo "Segredo indisponivel: $DB_SECRET_NAME. Verifique ministack-init." >&2
    exit 1
  fi
  sleep 2
done
DB_USERNAME=$(printf '%s' "$secret" | jq -er .username)
DB_PASSWORD=$(printf '%s' "$secret" | jq -er .password)
export DB_USERNAME DB_PASSWORD
unset secret
exec "$@"
