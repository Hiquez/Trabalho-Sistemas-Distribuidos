#!/bin/sh
set -eu
secret=$(aws --endpoint-url "$SECRETS_ENDPOINT" secretsmanager get-secret-value --secret-id "$DB_SECRET_NAME" --query SecretString --output text)
DB_USERNAME=$(printf '%s' "$secret" | jq -er .username)
DB_PASSWORD=$(printf '%s' "$secret" | jq -er .password)
export DB_USERNAME DB_PASSWORD
unset secret
exec "$@"
