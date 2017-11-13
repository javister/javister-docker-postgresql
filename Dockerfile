FROM javister-docker-docker.bintray.io/javister/javister-docker-base:1.0
MAINTAINER Viktor Verbitsky <vektory79@gmail.com>

ARG PG_MAJOR="9.5"
ARG PG_MJR="95"

LABEL postgresql=${PG_MAJOR}

ENV HOME="/root" \
    PG_MAJOR=${PG_MAJOR} \
    PG_MJR=${PG_MJR} \
    PGCONF="/config/postgres" \
    PGDATA="/config/postgres/databases" \
    PG_DB_NAME="system" \
    PGSETUP_INITDB_OPTIONS="--locale=ru_RU.UTF-8" \
    PGENGINE="/usr/pgsql-${PG_MAJOR}/bin" \
    PATH="/usr/pgsql-${PG_MAJOR}/bin:${PATH}" \
    RPMLIST="postgresql${PG_MJR} postgresql${PG_MJR}-server postgresql95-contrib"

COPY files /

RUN . /usr/local/sbin/yum-proxy && \
    yum install -y https://download.postgresql.org/pub/repos/yum/${PG_MAJOR}/redhat/rhel-7-x86_64/pgdg-centos${PG_MJR}-${PG_MAJOR}-3.noarch.rpm && \
    yum-install && \
    yum-clean && \
    chmod --recursive --changes +x /etc/my_init.d/*.sh /etc/service /usr/local/bin

HEALTHCHECK --interval=5s --timeout=3s --start-period=1m \
    CMD PGPASSWORD=${POSTGRES_PASSWORD} psql --dbname=${PG_DB_NAME} --host=$(getip) --port=5432 --username=system --command="select 1;" || exit 1

EXPOSE 5432
