# Docker образ PostgreSQL на основе образа [javister-docker-base](https://github.com/javister/javister-docker-base)

[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=9.5-1.1) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/9.5-1.1/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=9.6-1.1) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/9.6-1.1/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=11-1.1) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/11-1.1/link)
[ ![Download](https://api.bintray.com/packages/javister/docker/javister%3Ajavister-docker-postgresql/images/download.svg?version=12-1.1) ](https://bintray.com/javister/docker/javister%3Ajavister-docker-postgresql/12-1.1/link)
[ ![Download](https://api.bintray.com/packages/javister/dockertesting/javister-docker-postgresql/images/download.svg) ](https://bintray.com/javister/dockertesting/javister-docker-postgresql/_latestVersion)
![Build master branch](https://github.com/javister/javister-docker-postgresql/workflows/Build%20master%20branch/badge.svg)

Переменные окружения для настройки:

* PG_DB_NAME - имя БД, создаваемой при старте контейнера (если каталог пустой)
* POSTGRES_PASSWORD - пароль скперпользователя (system), который будет установлен для доступа к БД при инициализации