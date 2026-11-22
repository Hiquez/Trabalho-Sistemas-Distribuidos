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
