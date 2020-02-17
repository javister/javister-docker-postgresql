FROM javister-docker-docker.bintray.io/javister/javister-docker-base:1.1
MAINTAINER Viktor Verbitsky <vektory79@gmail.com>

ARG PG_VERSION="12"

LABEL postgresql=${PG_VERSION}

ENV HOME="/root" \
    PG_VERSION=${PG_VERSION} \
    PGCONF="/config/postgres" \
    PGDATA="/config/postgres/databases" \
    PG_DB_NAME="system" \
    PGSETUP_INITDB_OPTIONS="--locale=$LANG" \
    PGENGINE="/usr/pgsql-${PG_VERSION}/bin" \
    PG_FSYNC="on" \
    PG_SYNCHRONOUS_COMMIT="on" \
    PATH="/usr/pgsql-${PG_VERSION}/bin:${PATH}" \
    RPMLIST="postgresql${PG_VERSION} postgresql${PG_VERSION}-server postgresql${PG_VERSION}-contrib"

COPY files /

RUN . /usr/local/bin/yum-proxy && \
    yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm && \
    yum-install && \
    yum-clean && \
    chmod --recursive --changes +x /etc/my_init.d/*.sh /etc/service /usr/local/bin

HEALTHCHECK --interval=5s --timeout=3s --start-period=1m \
    CMD PGPASSWORD=${POSTGRES_PASSWORD} psql --dbname=${PG_DB_NAME} --host=$(getip) --port=5432 --username=system --command="select 1;" || exit 1

EXPOSE 5432
