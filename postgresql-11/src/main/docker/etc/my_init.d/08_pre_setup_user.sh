#!/bin/bash -e

# Удаляем пользователя, пришедшего вместе с пакетом установки PostgreSQL.
# Вместо него будет переименован пользователь system
[[ "$(id -u system > /dev/null 2>&1; echo $?)" == "0" ]] && userdel -r postgres 2> /dev/null || true
# Форсируем имя пользователя, т.к. для PostgreSQL требуется именно это имя.
export PUSER="postgres"
