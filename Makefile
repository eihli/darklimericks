##
# Project Title
#
# @file
# @version 0.1

src = $(shell find web -type f -iname *.clj 2>/dev/null)

darklimericks.jar: $(src)
	cd web && clj -A:depstar -M:depstar -m hf.depstar.uberjar \
		darklimericks.jar -C -m com.darklimericks.server.core

FORCE:

build: FORCE
	cd web && clj -A:depstar -M:depstar -m hf.depstar.uberjar \
		darklimericks.jar -C -m com.darklimericks.server.core

push: FORCE
	rsync -aP web/darklimericks.jar root@165.227.16.47:/root/darklimericks/web/

certs:
	ssh root@darklimericks.com \
		certbot certonly -d darklimericks.com \
		--webroot --webroot-path /root/darklimericks/web/resources/public --keep
# end
