ARG JAVISTER_BASE_VERSION
FROM javister-ntp.artifactory-jcr.krista.ru/javister-docker-base:${JAVISTER_BASE_VERSION}
MAINTAINER Viktor Verbitsky <vektory79@gmail.com>

ARG PG_VERSION="9.5"
ARG PG_PKG_VERSION="95"

LABEL postgresql=${PG_VERSION}

ENV PUSER="postgres" \
    HOME="/root" \
    PG_VERSION=${PG_VERSION} \
    POSTGRES_USER="sysdba" \
    PGCONF="/config/postgres" \
    PGDATA="/config/postgres/databases" \
    PG_DB_NAME="system" \
    PGSETUP_INITDB_OPTIONS="--locale=$LANG" \
    PGENGINE="/usr/pgsql-${PG_VERSION}/bin" \
    PG_FSYNC="on" \
    PG_SYNCHRONOUS_COMMIT="on" \
    PATH="/usr/pgsql-${PG_VERSION}/bin:${PATH}" \
    RPMLIST="postgresql${PG_PKG_VERSION} postgresql${PG_PKG_VERSION}-server postgresql${PG_PKG_VERSION}-contrib"

COPY src/main/docker /

RUN . /usr/local/bin/yum-proxy && \
    yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm && \
    yum-install && \
    yum-clean && \
    chmod --recursive --changes +x /etc/my_init.d/*.sh /etc/service /usr/local/bin

HEALTHCHECK --interval=5s --timeout=3s --start-period=1m \
    CMD PGPASSWORD=${POSTGRES_PASSWORD} psql --dbname=${PG_DB_NAME} --host=$(getip) --port=5432 --username=${POSTGRES_USER} --command="select 1;" || exit 1

EXPOSE 5432
