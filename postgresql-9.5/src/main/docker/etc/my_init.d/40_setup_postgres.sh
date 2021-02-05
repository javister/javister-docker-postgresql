#!/bin/bash
set -e

set_listen_addresses() {
	sedEscapedValue="$(echo "$1" | sed 's/[\/&]/\\&/g')"
	sed -ri "s/^#?(listen_addresses\s*=\s*)\S+/\1'$sedEscapedValue'/" "${PGDATA}/postgresql.conf"
}

chown -R ${PUSER}:${PUSER} "/config" || true

# create folder structure and change ownership
if [[ ! -d  "${PGDATA}" ]]; then

    mkdir -p "${PGDATA}"
    chown -R ${PUSER}:${PUSER} "${PGDATA}"

    # initialise empty database structure and change temporary ownership config files
    if [[ ! -d "${PGDATA}/${PG_VERSION}" ]]; then
        if [ "${LANG}" ]; then
            setuser ${PUSER} initdb --locale=${LANG}
        else 
            setuser ${PUSER} initdb
        fi
    fi
    if [[ "${POSTGRES_PASSWORD}" ]]; then
        pass="PASSWORD '${POSTGRES_PASSWORD}'"
        authMethod=md5
    else
        # The - option suppresses leading tabs but *not* spaces. :)
        MESSAGE=$(cat <<-'EOWARN'
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
)
        mwarn "${MESSAGE}"

        pass=
        authMethod=trust
    fi
    
    { echo; echo "host all all 0.0.0.0/0 $authMethod"; } >> "${PGDATA}/pg_hba.conf"
    set_listen_addresses '127.0.0.1'

    sync

    setuser ${PUSER} postgres &
    pid="$!"
    
    wait4tcp 127.0.0.1 5432
    sleep 5

    setuser ${PUSER} createdb ${PG_DB_NAME}
    setuser ${PUSER} psql --dbname=${PG_DB_NAME} --command "CREATE ROLE ${POSTGRES_USER} WITH LOGIN ${pass} SUPERUSER;"

    setuser ${PUSER} pg_ctl stop -w

    set_listen_addresses '*'

    sed -ri "s/^#?(log_destination\s*=\s*)\S+(\s+.*)$/\1'stderr,syslog'\2/" "${PGDATA}/postgresql.conf"
    sed -ri "s/^#?(fsync\s*=\s*)\S+(\s+.*)$/\1${PG_FSYNC}\2/" "${PGDATA}/postgresql.conf"
    sed -ri "s/^#?(synchronous_commit\s*=\s*)\S+(\s+.*)$/\1${PG_SYNCHRONOUS_COMMIT}\2/" "${PGDATA}/postgresql.conf"

    sync
fi

if [[ ! -f "${PGCONF}/postgresql.conf" ]]; then
    cp "${PGDATA}"/postgresql.conf "${PGCONF}"/postgresql.conf
    chown ${PUSER}:${PUSER} "${PGCONF}"/postgresql.conf
    chmod 666 "${PGCONF}"/postgresql.conf
fi

if [[ ! -f "${PGCONF}/backup" ]]; then
    mkdir -p "${PGCONF}/backup"
    chown ${PUSER}:${PUSER} "${PGCONF}/backup"
fi

chown -R ${PUSER}:${PUSER} "${PGCONF}" || true
