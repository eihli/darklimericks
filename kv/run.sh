#!/usr/bin/env sh
set -euo pipefail

KVDATA=${KVDATA:-"$(pwd)/data/dev"}

docker run \
    -p 127.0.0.1:6379:6379 \
    -v $KVDATA:/data \
    --name kv \
    darklimericks-kv --appendonly yes
