package com.github.javister.docker.testing.postgresql;

import com.github.javister.docker.testing.base.JavisterBaseContainer;
import org.testcontainers.containers.BindMode;

import java.io.File;
import java.time.Duration;

/**
 * Обёртка над контейнером
 * <a href="https://github.com/javister/javister-docker-postgresql">
 * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
 * </a>.
 *
 * <p>Образ данного контейнера содержит базу данных PostgreSQL.
 *
 * <p>squid:S00119 - данная нотация диктуется библиотекой testcontainers.
 *
 * @param <SELF> параметр, необходимый для организации паттерна fluent API.
 */
@SuppressWarnings({"squid:S00119", "UnusedReturnValue", "unused"})
public class JavisterPostgreSQLContainer<SELF extends JavisterPostgreSQLContainer<SELF>> extends JavisterBaseContainer<SELF> {
    /**
     * Создаёт контейнер из образа
     * <a href="https://github.com/javister/javister-docker-postgresql">
     * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
     * </a> и монтирует к нему каталог по указанному пути.
     *
     * @param volumePath путь к каталогу, который необходимо примонтировать к контейнеру.
     */
    public JavisterPostgreSQLContainer(File volumePath) {
        this(Version.V12, volumePath);
    }

    /**
     * Создаёт контейнер из образа
     * <a href="https://github.com/javister/javister-docker-postgresql">
     * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
     * </a> и монтирует к нему каталог по указанному пути.
     *
     * @param version    версия релиза PostgreSQL
     * @param volumePath путь к каталогу, который необходимо примонтировать к контейнеру.
     */
    public JavisterPostgreSQLContainer(Version version, File volumePath) {
        super(
                getImageRepository(JavisterPostgreSQLContainer.class, version.getValue()),
                getImageTag(JavisterPostgreSQLContainer.class, version.getValue())
        );
        init(volumePath);
    }

    /**
     * Создаёт контейнер с базой данных PostgreSQL для JUnit тестирования.
     *
     * <p>Объект класса необходим для нахождения рабочего каталога тестов.
     *
     * <p>squid:S1699 - В общем случае так делать не хорошо, но в данном конкретном месте побочные эффекты учнеты.
     *
     * @param version   версия релиза PostgreSQL
     * @param testClass класс JUnit теста для которого создаётся контейнер.
     */
    @SuppressWarnings("squid:S1699")
    public JavisterPostgreSQLContainer(Version version, Class<?> testClass) {
        super(
                getImageRepository(JavisterPostgreSQLContainer.class, version.getValue()),
                getImageTag(JavisterPostgreSQLContainer.class, version.getValue()),
                testClass);
        init(getTestVolumePath());
    }

    /**
     * Создаёт контейнер с базой данных PostgreSQL для JUnit тестирования.
     *
     * <p>Объект класса необходим для нахождения рабочего каталога тестов.
     *
     * <p>squid:S1699 - В общем случае так делать не хорошо, но в данном конкретном месте побочные эффекты учнеты.
     *
     * @param testClass класс JUnit теста для которого создаётся контейнер.
     */
    @SuppressWarnings("squid:S1699")
    public JavisterPostgreSQLContainer(Class<?> testClass) {
        this(Version.V12, testClass);
    }

    /**
     * Задаёт имя базы данных, создаваемой при инициализации контейнера.
     * <p>Если в примонтированном каталоге уже есть данные, то новая БД создаваться не буде, и контейнер запустится с
     * имеющимися данными.
     *
     * @param dbName имя базы данных.
     * @return возвращает this для fluent API.
     */
    public SELF withDbName(String dbName) {
        this.withEnv("PG_DB_NAME", dbName);
        return self();
    }

    /**
     * Включает или отключает файловую синхронизацию БД.
     *
     * <p>Крайне не рекомендуется отключать эту синхронизацию на продакшене, но при тестировании это позволяет
     * существенно сократить время тестирования.
     *
     * @param fSync true для включения синхронизации ( значение по умолчанию) и false в противном случае.
     * @return возвращает this для fluent API.
     */
    public SELF withFSync(boolean fSync) {
        this.withEnv("PG_FSYNC", boolToOnOff(fSync));
        return self();
    }

    /**
     * Включает или отключает файловую синхронизацию для коммитов.
     *
     * <p>Данную опцию можно выключить, если не страшно потерять последние транзакции в случае сбоя. Это даёт
     * выигрыш в производительности, но не такой сильный, как при отключении
     * {@link JavisterPostgreSQLContainer#withFSync(boolean)}.
     *
     * @param sCommit true для включения синхронизации ( значение по умолчанию) и false в противном случае.
     * @return возвращает this для fluent API.
     */
    public SELF withSynchronousCommit(boolean sCommit) {
        this.withEnv("PG_SYNCHRONOUS_COMMIT", boolToOnOff(sCommit));
        return self();
    }

    /**
     * Устанавливает пароль на БД при инициализации контейнера.
     * <p>Если в примонтированном каталоге уже есть данные, то новый пароль задаваться не буде, и контейнер запустится с
     * имеющимися данными.
     *
     * @param password пароль на БД.
     * @return возвращает this для fluent API.
     */
    public SELF withPostgresPassword(String password) {
        this.withEnv("POSTGRES_PASSWORD", password);
        return self();
    }

    private void init(File volumePath) {
        if (volumePath != null) {
            this.withFileSystemBind(volumePath.toString(), "/config/postgres", BindMode.READ_WRITE);
        }
        this
                .withExposedPorts(5432)
                .withStartupTimeout(Duration.ofMinutes(3))
                .withLogPrefix("POSTGRE");
    }

    public enum Version {
        V12("12");
        private final String value;

        Version(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
