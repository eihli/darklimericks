#!/usr/bin/env sh

PGDATA=${PGDATA:-"$(pwd)/data/dev"}
POSTGRES_USER=${POSTGRES_USER:-"dev"}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-"dev"}

docker run \
    --name db \
    -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
    -e POSTGRES_USER=$POSTGRES_USER \
    -v $PGDATA:/var/lib/postgresql/data \
    -p 5432:5432 \
    darklimericks-db
