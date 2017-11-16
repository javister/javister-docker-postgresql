#!/bin/bash

## resolve links - $0 may be a link to ant's home
PRG="$0"

# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls="$(ls -ld "$PRG")"
  link="$(expr "$ls" : '.*-> \(.*\)$')"
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$PRG")/$link"
  fi
done

PROXY_ARGS="--env http_proxy=${http_proxy} \
            --env no_proxy=${no_proxy}"

WORK_DIR="$(dirname "$PRG")"
WORK_DIR="$(readlink -f ${WORK_DIR})/tmp"

mkdir -p ${WORK_DIR}
sync

GID="$(id -g)"

docker run -it --name postgresql -p 5432:5432 --env PUID=$UID --env PGID=${GID} --rm ${PROXY_ARGS} -v ${WORK_DIR}:/config/postgres:rw -e POSTGRES_PASSWORD=postgres javister-docker-docker.bintray.io/javister/javister-docker-postgresql:9.5 $@
