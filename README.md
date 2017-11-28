# Docker образ PostgreSQL на основе образа [javister-docker-base](https://github.com/javister/javister-docker-base)

[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/_latestVersion)
[![Build Status](https://travis-ci.org/javister/javister-docker-postgresql.svg?branch=master)](https://travis-ci.org/javister/javister-docker-postgresql)

Переменные окружения для настройки:

* PG_DB_NAME - имя БД, создаваемой при старте контейнера (если каталог пустой)
* POSTGRES_PASSWORD - пароль скперпользователя (system), который будет установлен для доступа к БД при инициализации