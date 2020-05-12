# Docker образ PostgreSQL на основе образа [javister-docker-base](https://github.com/javister/javister-docker-base)

[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=9.5-1.2) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/9.5-1.2/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=9.6-1.2) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/9.6-1.2/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=11-1.2) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/11-1.2/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=12-1.2) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/12-1.2/link)
[ ![Download](https://api.bintray.com/packages/javister/dockertesting/javister-docker-postgresql/images/download.svg) ](https://bintray.com/javister/dockertesting/javister-docker-postgresql/_latestVersion)
![Build master branch](https://github.com/javister/javister-docker-postgresql/workflows/Build%20master%20branch/badge.svg)

Переменные окружения для настройки:

* PG_DB_NAME - имя БД, создаваемой при старте контейнера (если каталог пустой)
* POSTGRES_USER - пользователь - владелец БД (по умолчанию sysdba)
* POSTGRES_PASSWORD - пароль суперпользователя (по умолчанию postgres), который будет установлен для доступа к БД при инициализации
* PG_FSYNC - `on` (по умолчанию) для включения синхронной записи на диск и `off` - для отключения
* PG_SYNCHRONOUS_COMMIT - `on` (по умолчанию) для включения синхронизации при коммите и `off` - для отключения

`PUSER` зафиксирован как `postgres`, т.к. это требуется для корректной работы БД. 
