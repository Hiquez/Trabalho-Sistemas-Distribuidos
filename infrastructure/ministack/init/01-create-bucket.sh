#!/bin/sh
set -eu

ENDPOINT="${S3_ENDPOINT:-http://ministack:4566}"
BUCKET="${S3_BUCKET:-coleta-comprovantes}"

# O emulador pode demorar alguns segundos para aceitar requisicoes.
i=0
until aws --endpoint-url "$ENDPOINT" s3api list-buckets >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -ge 60 ]; then
    echo "MiniStack nao ficou disponivel em $ENDPOINT" >&2
    exit 1
  fi
  sleep 2
done

if aws --endpoint-url "$ENDPOINT" s3api head-bucket --bucket "$BUCKET" >/dev/null 2>&1; then
  echo "Bucket existente: $BUCKET"
else
  aws --endpoint-url "$ENDPOINT" s3api create-bucket --bucket "$BUCKET"     --region "${AWS_DEFAULT_REGION:-us-east-1}"
  echo "Bucket criado: $BUCKET"
fi

create_secret() {
  name="$1"; username="$2"; password="$3"
  case "$password" in *[!a-zA-Z0-9_]*) echo 'Senha invalida' >&2; exit 1;; esac
  [ -n "$password" ]
  value=$(printf '{"username":"%s","password":"%s"}' "$username" "$password")
  if aws --endpoint-url "$ENDPOINT" secretsmanager describe-secret --secret-id "$name" >/dev/null 2>&1; then
    aws --endpoint-url "$ENDPOINT" secretsmanager put-secret-value --secret-id "$name" --secret-string "$value" >/dev/null
  else
    aws --endpoint-url "$ENDPOINT" secretsmanager create-secret --name "$name" --secret-string "$value" >/dev/null
  fi
  aws --endpoint-url "$ENDPOINT" secretsmanager get-secret-value --secret-id "$name" --query ARN --output text
}
create_secret coleta/solicitacoes SOLICITACOES "$SOLICITACOES_PASSWORD"
create_secret coleta/agenda AGENDA_CAPACIDADE "$AGENDA_CAPACIDADE_PASSWORD"
create_secret coleta/cooperativa COOPERATIVA "$COOPERATIVA_PASSWORD"
create_secret coleta/coletas COLETAS_DESTINACAO "$COLETAS_DESTINACAO_PASSWORD"
