FROM javister-docker-docker.bintray.io/javister/javister-docker-base:1.0
MAINTAINER Viktor Verbitsky <vektory79@gmail.com>
LABEL postgresql=9.5

COPY files /

ENV PG_MAJOR="9.5"
ENV HOME="/root" \
    PGCONF="/config/postgres" \
    PGDATA="/config/postgres/databases" \
    PG_DB_NAME="system" \
    PGSETUP_INITDB_OPTIONS="--locale=ru_RU.UTF-8" \
    PGENGINE="/usr/pgsql-9.5/bin" \
    PATH="/usr/pgsql-$PG_MAJOR/bin:$PATH" \
    RPMLIST="postgresql95 postgresql95-server postgresql95-contrib"

RUN . /usr/local/sbin/yum-proxy && \
    yum install -y https://download.postgresql.org/pub/repos/yum/9.5/redhat/rhel-7-x86_64/pgdg-centos95-9.5-3.noarch.rpm && \
    yum-install && \
    yum-clean && \
    chmod --recursive --changes +x /etc/my_init.d/*.sh /etc/service /usr/local/bin

EXPOSE 5432
