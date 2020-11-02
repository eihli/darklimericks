#!/usr/bin/env bash
set -euo pipefail

# Run once as part of Docker build script to initialize
# the database with encryption for user passwords.

POSTGRES_USER=${POSTGRES_USER:-"dev"}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-"dev"}

psql -v ON_ERROR_STOP=1 \
     --username "$POSTGRES_USER" \
     --dbname "$POSTGRES_DB" <<-EOSQL
     SET password_encryption = "scram-sha-256";
     ALTER USER "$POSTGRES_USER" WITH ENCRYPTED PASSWORD '$POSTGRES_PASSWORD';
EOSQL
