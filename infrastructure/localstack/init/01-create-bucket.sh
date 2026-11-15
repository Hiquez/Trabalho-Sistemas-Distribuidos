#!/bin/bash
set -e

BUCKET="coleta-comprovantes"

if awslocal s3api head-bucket --bucket "$BUCKET" 2>/dev/null; then
    echo "Bucket ja existe: $BUCKET"
else
    awslocal s3 mb "s3://$BUCKET"
    echo "Bucket criado: $BUCKET"
fi