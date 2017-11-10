# Docker образ PostgreSQL на основе образа [javister-docker-base](https://github.com/javister/javister-docker-base)

Переменные окружения для настройки:

* PG_DB_NAME - имя БД, создаваемой при старте контейнера (если каталог пустой)
* POSTGRES_PASSWORD - пароль скперпользователя (system), который будет установлен для доступа к БД при инициализации