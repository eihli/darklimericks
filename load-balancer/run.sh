#!/bin/sh

HOST_IP=`ip -4 addr show scope global dev docker0 | grep inet | awk '{print \$2}' | cut -d / -f 1`
FULLCHAIN_PEM="${FULLCHAIN_PEM:-/etc/letsencrypt/live/darklimericks.com/fullchain.pem}"

docker run \
    --add-host darklimericks:$HOST_IP \
    -p 80:80 -p 443:443 \
    --sysctl net.ipv4.ip_unprivileged_port_start=0 \
    --mount type=bind,source=$FULLCHAIN_PEM,target=/etc/ssl/cert.pem \
    --mount type=bind,source=$(pwd)/haproxy.cfg,target=/usr/local/etc/haproxy/haproxy.cfg \
    --rm -it haproxy
