#!/bin/bash
mkdir -p /var/run/postgresql
chown -R system:system /var/run/postgresql

if [ -f "${PGCONF}/postgresql.conf" ]; then
    chown system:system "${PGCONF}"/postgresql.conf
    chmod 666 "${PGCONF}"/postgresql.conf
fi
