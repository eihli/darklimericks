##
# Project Title
#
# @file
# @version 0.1

src = $(shell find web -type f -iname *.clj 2>/dev/null)

LottoEmail.jar: $(src)
	cd web && clj -A:depstar -M:depstar -m hf.depstar.uberjar \
		darklimericks.jar -C -m com.darklimericks.server.core

build: FORCE
	cd web && clj -A:depstar -M:depstar -m hf.depstar.uberjar \
		darklimericks.jar -C -m com.darklimericks.server.core

FORCE:

push: FORCE
	rsync -aP web/LottoEmail.jar root@165.227.27.168:/root

deploy: LottoEmail.jar
	ssh root@165.227.27.168 java -jar /root/LottoEmail.jar
# end
