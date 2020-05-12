#!/bin/bash
mkdir -p /var/run/postgresql
chown -R ${PUSER}:${PUSER} /var/run/postgresql

if [[ -f "${PGCONF}/postgresql.conf" ]]; then
    chown ${PUSER}:${PUSER} "${PGCONF}"/postgresql.conf
    chmod 666 "${PGCONF}"/postgresql.conf
fi
