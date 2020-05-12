#!/bin/bash -e

# Удаляем пользователя, пришедшего вместе с пакетом установки PostgreSQL.
# Вместо него будет переименован пользователь system
userdel -r postgres 2> /dev/null
# Форсируем имя пользователя, т.к. для PostgreSQL требуется именно это имя.
export PUSER="postgres"
