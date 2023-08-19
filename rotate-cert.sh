#!/usr/bin/env sh

cd darklimericks/load-balancer
docker stop haproxy
docker rm haproxy
cat /etc/letsencrypt/live/darklimericks.com/privkey.pem >> /etc/letsencrypt/live/darklimericks.com/fullchain.pem
./run.sh
