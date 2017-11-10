#!/bin/bash
set -e

set_listen_addresses() {
	sedEscapedValue="$(echo "*" | sed 's/[\/&]/\\&/g')"
	sed -ri "s/^#?(listen_addresses\s*=\s*)\S+/\1'$sedEscapedValue'/" "${PGDATA}/postgresql.conf"
}

chown -R system:system "/config"

# create folder structure and change ownership
if [ ! -d  "${PGDATA}" ]; then

    mkdir -p "${PGDATA}"
    chown -R system:system "${PGDATA}"

    # initialise empty database structure and change temporary ownership config files
    if [ ! -d "${PGDATA}/${PG_MAJOR}" ]; then
        setuser system initdb --locale=ru_RU.UTF-8
    fi
    if [ "${POSTGRES_PASSWORD}" ]; then
        pass="PASSWORD '${POSTGRES_PASSWORD}'"
        authMethod=md5
    else
        # The - option suppresses leading tabs but *not* spaces. :)
        cat >&2 <<-'EOWARN'
****************************************************
WARNING: No password has been set for the database.
This will allow anyone with access to the
Postgres port to access your database. In
Docker's default configuration, this is
effectively any other container on the same
system.
Use "-e POSTGRES_PASSWORD=password" to set
it in "docker run".
****************************************************
EOWARN

        pass=
        authMethod=trust
    fi
    
    { echo; echo "host all all 0.0.0.0/0 $authMethod"; } >> "${PGDATA}/pg_hba.conf"
    set_listen_addresses ''

    setuser system postgres >/dev/null 2>&1 &
    pid="$!"
    
    wait4tcp $(getip) 5432

    setuser system createdb ${PG_DB_NAME} >/dev/null 2>&1
    setuser system psql --command "ALTER USER system WITH SUPERUSER $pass;"  >/dev/null 2>&1
    
    setuser system pg_ctl stop -w

    set_listen_addresses '*'

    sed -ri "s/^log_destination = 'stderr'(.*)$/log_destination = 'stderr,syslog'\1/" "${PGDATA}/postgresql.conf"
    sync
fi

if [ ! -f "${PGCONF}/postgresql.conf" ]; then
    cp "${PGDATA}"/postgresql.conf "${PGCONF}"/postgresql.conf
    chown system:system "${PGCONF}"/postgresql.conf
    chmod 666 "${PGCONF}"/postgresql.conf
fi

if [ ! -f "${PGCONF}/backup" ]; then
    mkdir -p "${PGCONF}/backup"
    chown system:system "${PGCONF}/backup"
fi

chown -R system:system "${PGCONF}"
