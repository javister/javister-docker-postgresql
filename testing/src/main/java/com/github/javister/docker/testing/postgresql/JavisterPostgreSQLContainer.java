package com.github.javister.docker.testing.postgresql;

import com.github.javister.docker.testing.IllegalExecResultException;
import com.github.javister.docker.testing.IllegalImageVariantException;
import com.github.javister.docker.testing.base.JavisterBaseContainer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

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
@SuppressWarnings({"squid:S00119", "UnusedReturnValue", "unused", "java:S2160"})
public class JavisterPostgreSQLContainer<SELF extends JavisterPostgreSQLContainer<SELF>> extends PostgreSQLContainer<SELF> implements JavisterBaseContainer<SELF> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavisterPostgreSQLContainer.class);

    public static final String JAVISTER_DRIVER_NAME = "javisterpsql";
    public static final String IMAGE = JavisterBaseContainer.getImageRepository(JavisterPostgreSQLContainer.class, Variant.V12.getValue());
    public static final String DEFAULT_TAG = JavisterBaseContainer.getImageTag(JavisterPostgreSQLContainer.class, Variant.V12.getValue());
    public static final Integer POSTGRESQL_PORT = 5432;

    private Class<?> testClass;
    protected String logPrefix = "DOCKER";
    private boolean suppressSlfLogger = false;
    private final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER).withPrefix(logPrefix).withRemoveAnsiCodes(false);
    private Variant variant;
    private File volumePath;

    /**
     * Создаёт контейнер из образа
     * <a href="https://github.com/javister/javister-docker-postgresql">
     * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
     * </a> и монтирует к нему каталог по указанному пути.
     *
     * @param volumePath путь к каталогу, который необходимо примонтировать к контейнеру.
     */
    public JavisterPostgreSQLContainer(File volumePath) {
        this(Variant.V12, volumePath);
    }

    /**
     * Создаёт контейнер из образа
     * <a href="https://github.com/javister/javister-docker-postgresql">
     * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
     * </a>.
     *
     * @param variant версия релиза PostgreSQL
     */
    public JavisterPostgreSQLContainer(Variant variant) {
        super(
                JavisterBaseContainer.getImageRepository(JavisterPostgreSQLContainer.class, variant.getValue()) +
                        ":" +
                        JavisterBaseContainer.getImageTag(JavisterPostgreSQLContainer.class, variant.getValue())
        );
        this.variant = variant;
        init(null);
    }

    /**
     * Создаёт контейнер из образа
     * <a href="https://github.com/javister/javister-docker-postgresql">
     * javister-docker-docker.bintray.io/javister/javister-docker-postgresql
     * </a> и монтирует к нему каталог по указанному пути.
     *
     * @param variant    версия релиза PostgreSQL
     * @param volumePath путь к каталогу, который необходимо примонтировать к контейнеру.
     */
    public JavisterPostgreSQLContainer(Variant variant, File volumePath) {
        super(
                JavisterBaseContainer.getImageRepository(JavisterPostgreSQLContainer.class, variant.getValue()) +
                        ":" +
                        JavisterBaseContainer.getImageTag(JavisterPostgreSQLContainer.class, variant.getValue())
        );
        this.variant = variant;
        init(volumePath);
    }

    /**
     * Создаёт контейнер с базой данных PostgreSQL для JUnit тестирования.
     *
     * <p>Объект класса необходим для нахождения рабочего каталога тестов.
     *
     * <p>squid:S1699 - В общем случае так делать не хорошо, но в данном конкретном месте побочные эффекты учнеты.
     *
     * @param variant   версия релиза PostgreSQL
     * @param testClass класс JUnit теста для которого создаётся контейнер.
     */
    @SuppressWarnings("squid:S1699")
    public JavisterPostgreSQLContainer(Variant variant, Class<?> testClass) {
        super(
                JavisterBaseContainer.getImageRepository(JavisterPostgreSQLContainer.class, variant.getValue()) +
                        ":" +
                        JavisterBaseContainer.getImageTag(JavisterPostgreSQLContainer.class, variant.getValue()));
        this.variant = variant;
        this.testClass = testClass;
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
        this(Variant.V12, testClass);
    }

    @Override
    protected void configure() {
        addExposedPort(POSTGRESQL_PORT);
        this.withEnv("POSTGRES_PASSWORD", getPassword());
        this.withEnv("PG_DB_NAME", getDatabaseName());
        if (volumePath != null) {
            this.withFileSystemBind(volumePath.toString(), "/config/postgres", BindMode.READ_WRITE);
        }
    }

    @Override
    @Nullable
    public Class<?> getTestClass() {
        return testClass;
    }

    @NotNull
    @Override
    public String getLogPrefix() {
        return logPrefix;
    }

    @Override
    public void setLogPrefix(@NotNull String logPrefix) {
        this.logPrefix = logPrefix;
    }

    @Override
    public boolean isSuppressSlfLogger() {
        return suppressSlfLogger;
    }

    @Override
    public void setSuppressSlfLogger(boolean suppressSlfLogger) {
        this.suppressSlfLogger = suppressSlfLogger;
    }

    @Nullable
    @Override
    public String getVariant() {
        return variant.value;
    }

    @NotNull
    @Override
    public SELF withNetwork(@NotNull Network network) {
        // Форсируем создание сети
        network.getId();
        getInternalDependencies().forEach(it -> it.withNetwork(network));
        return super.withNetwork(network);
    }

    @Override
    public void setNetwork(@NotNull Network network) {
        network.getId();
        getInternalDependencies().forEach(it -> it.setNetwork(network));
        super.setNetwork(network);
    }

    /**
     * Задаёт имя базы данных, создаваемой при инициализации контейнера.
     * <p>Если в примонтированном каталоге уже есть данные, то новая БД создаваться не буде, и контейнер запустится с
     * имеющимися данными.
     *
     * @param dbName имя базы данных.
     * @return возвращает this для fluent API.
     * @deprecated use {@link #withDatabaseName(String)}
     */
    @Deprecated
    public SELF withDbName(String dbName) {
        return withDatabaseName(dbName);
    }

    @Override
    public String getUsername() {
        return "system";
    }

    @Override
    public SELF withUsername(final String username) {
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
        return this.withEnv("PG_FSYNC", JavisterBaseContainer.boolToOnOff(fSync));
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
        return this.withEnv("PG_SYNCHRONOUS_COMMIT", JavisterBaseContainer.boolToOnOff(sCommit));
    }

    /**
     * Устанавливает пароль на БД при инициализации контейнера.
     * <p>Если в примонтированном каталоге уже есть данные, то новый пароль задаваться не буде, и контейнер запустится с
     * имеющимися данными.
     *
     * @param password пароль на БД.
     * @return возвращает this для fluent API.
     * @deprecated use {@link #withPassword(String)}
     */
    @Deprecated
    public SELF withPostgresPassword(String password) {
        return withPassword(password);
    }

    public File getVolumePath() {
        return volumePath;
    }

    public SELF withVolumePath(File volumePath) {
        this.volumePath = volumePath;
        return self();
    }

    public SELF withVolumePath(String volumePath) {
        if (volumePath != null && !volumePath.isEmpty()) {
            withVolumePath(new File(volumePath));
        } else {
            this.volumePath = null;
        }
        return self();
    }

    public String backup() throws IOException, InterruptedException {
        ExecResult execResult = this.execInContainer("pg-backup");
        if (execResult.getExitCode() != 0) {
            throw new IllegalExecResultException("Executed command fails: " + execResult.getStderr());
        }
        return execResult.getStdout().replaceAll("^Backup to (backup-.*)$", "$1").trim();
    }

    public String backup(String fileName) throws IOException, InterruptedException {
        checkBackupName(fileName);
        ExecResult execResult = this.execInContainer("pg-backup", fileName);
        if (execResult.getExitCode() != 0) {
            throw new IllegalExecResultException("Executed command fails: " + execResult.getStderr());
        }
        return execResult.getStdout().replaceAll("^Backup to (" + fileName.replace(".", "\\.") + ")$", "$1").trim();
    }

    public void restore(String fileName) throws IOException, InterruptedException {
        checkBackupName(fileName);
        ExecResult execResult = this.execInContainer("pg-restore", fileName);
        System.out.println(execResult.getStdout());
        if (execResult.getExitCode() != 0) {
            throw new IllegalExecResultException("Executed command fails: " + execResult.getStderr());
        }
    }

    public void performQuery(String sql, SqlConsumer<ResultSet> processor) throws SQLException {
        try (HikariDataSource ds = getDataSource(); Statement statement = ds.getConnection().createStatement()) {
            statement.execute(sql);
            try (ResultSet resultSet = statement.getResultSet()) {

                resultSet.next();
                processor.accept(resultSet);
            }
        }
    }

    public HikariDataSource getDataSource() {
        return getDataSource(1);
    }

    public HikariDataSource getDataSource(int poolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrl());
        hikariConfig.setUsername(getUsername());
        hikariConfig.setPassword(getPassword());
        hikariConfig.setDriverClassName(getDriverClassName());
        hikariConfig.setMaximumPoolSize(poolSize);
        return new HikariDataSource(hikariConfig);
    }

    public static void performQuery(String jdbcUrl, String sql, SqlConsumer<ResultSet> processor) throws SQLException {
        try (HikariDataSource ds = getDataSource(jdbcUrl); Statement statement = ds.getConnection().createStatement()) {
            statement.execute(sql);
            try (ResultSet resultSet = statement.getResultSet()) {

                resultSet.next();
                processor.accept(resultSet);
            }
        }
    }

    public static HikariDataSource getDataSource(String jdbcUrl) {
        return getDataSource(jdbcUrl, 1);
    }

    public static HikariDataSource getDataSource(String jdbcUrl, int poolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(poolSize);

        return new HikariDataSource(hikariConfig);
    }

    @NotNull
    @Override
    public Slf4jLogConsumer getLogConsumer() {
        return logConsumer;
    }

    private void checkBackupName(String fileName) {
        if (fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("File name could contains a name only, without path. But name is: " + fileName);
        }
    }

    private void init(File volumePath) {
        initialize();
        this.volumePath = volumePath;
        this
                .withDatabaseName("system")
                .withExposedPorts(5432)
                .withStartupTimeout(Duration.ofMinutes(3))
                .withCommand("/usr/local/bin/my_init");
        this.waitStrategy = new DockerHealthcheckWaitStrategy()
                .withStartupTimeout(Duration.of(10, MINUTES));
    }

    public enum Variant {
        V9_5("9_5"),
        V9_6("9_6"),
        V11("11"),
        V12("12");
        private final String value;

        Variant(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Variant get(String value) {
            switch (value) {
                case "9_5":
                    return V9_5;
                case "9_6":
                    return V9_6;
                case "11":
                    return V11;
                case "12":
                    return V12;
                default:
                    throw new IllegalImageVariantException("Unexpected Docker variant " + value + " for image " + IMAGE);
            }
        }
    }

    /**
     * Represents an operation that accepts a single input argument and returns no
     * result or throws an exception.
     *
     * @param <T> the type of the input to the operation
     */
    @FunctionalInterface
    public interface SqlConsumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void accept(T t) throws SQLException;

        /**
         * Returns a composed {@code Consumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing either
         * operation throws an exception, it is relayed to the caller of the
         * composed operation.  If performing this operation throws an exception,
         * the {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code Consumer} that performs in sequence this
         * operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         */
        default java.util.function.Consumer<T> andThen(SqlConsumer<? super T> after) {
            Objects.requireNonNull(after);
            return (T t) -> {
                try {
                    accept(t);
                    after.accept(t);
                } catch (SQLException e) {
                    throw new SqlProcessingException(e);
                }
            };
        }
    }
}
