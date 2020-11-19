package com.github.javister.docker.testing.postgresql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectImageTests {

    @Order(0)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void simpleTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant)) {
            postgres.deleteTestDir();
            postgres.start();

            postgres.performQuery(
                    "SELECT 1",
                    resultSet -> assertEquals(1, resultSet.getInt(1), "A basic SELECT query succeeds")
            );
        }
    }

    @Order(1)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void nonResultingQueriesTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant).withInitScript("SimpleDB.sql")) {
            postgres.start();

            AtomicInteger counter = new AtomicInteger(0);
            postgres.performQuery(
                    "INSERT INTO bar (foo) VALUES ('test1')",
                    resultSet -> counter.getAndIncrement()
            );
            assertEquals(0, counter.get(), "Не должно было быть никаких результатов");
            postgres.performQuery("INSERT INTO bar (foo) VALUES ('test2')");
            postgres.performQuery(
                    "SELECT foo FROM bar WHERE foo = 'Non existed'",
                    resultSet -> counter.getAndIncrement()
            );
            assertEquals(0, counter.get(), "Не должно было быть никаких результатов");
            postgres.performQuery(
                    "SELECT foo FROM bar WHERE foo = 'test1'",
                    resultSet -> assertEquals("test1", resultSet.getString(1), "Должно присутствовать значение 'test1'")
            );
            postgres.performQuery(
                    "SELECT foo FROM bar WHERE foo = 'test2'",
                    resultSet -> assertEquals("test2", resultSet.getString(1), "Должно присутствовать значение 'test2'")
            );
        }
    }

    @Order(2)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void initDbTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant).withInitScript("SimpleDB.sql")) {
            postgres.deleteTestDir();
            postgres.start();

            postgres.performQuery(
                    "SELECT foo FROM bar",
                    resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
            );
        }
    }

    @Order(3)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void backupTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException, InterruptedException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant)) {
            postgres.start();

            postgres.performQuery(
                    "SELECT foo FROM bar",
                    resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
            );

            String backupName = postgres.backup();
            assertThat(backupName, matchesPattern("^backup-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.dump"));
            assertNotNull(postgres.getTestVolumePath());
            assertTrue(Files.exists(postgres.getTestVolumePath().toPath().resolve("backup").resolve(backupName)));

            String backupName2 = postgres.backup("test.backup");
            assertEquals("test.backup", backupName2);
            assertNotNull(postgres.getTestVolumePath());
            assertTrue(Files.exists(postgres.getTestVolumePath().toPath().resolve("backup").resolve(backupName2)));

            assertNotNull(postgres.getTestPath());
            Files.copy(
                    postgres.getTestVolumePath().toPath().resolve("backup").resolve("test.backup"),
                    postgres.getTestPath().toPath().resolve("test-" + variant.getValue() + ".backup"),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Order(4)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void restoreTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException, InterruptedException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant)) {
            postgres.deleteTestDir();
            postgres.start();

            assertNotNull(postgres.getTestPath());
            assertNotNull(postgres.getTestVolumePath());
            Files.copy(
                    postgres.getTestPath().toPath().resolve("test-" + variant.getValue() + ".backup"),
                    postgres.getTestVolumePath().toPath().resolve("backup").resolve("test.backup"),
                    StandardCopyOption.REPLACE_EXISTING);

            postgres.restore("test.backup");

            postgres.performQuery(
                    "SELECT foo FROM bar",
                    resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
            );
        }
    }

    @Order(4)
    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void withRestoreTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant)) {
            Assertions.assertNotNull(postgres.getTestPath(), "В тесте путь к рабочему каталогу должен быть установлен.");
            postgres.withExternalBackup(postgres.getTestPath().toPath().resolve("test-" + variant.getValue() + ".backup").toString());
            postgres.deleteTestDir();
            postgres.start();

            postgres.performQuery(
                    "SELECT foo FROM bar",
                    resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
            );
        }
    }

    private JavisterPostgreSQLContainer<?> getPostgre(JavisterPostgreSQLContainer.Variant variant) {
        return new JavisterPostgreSQLContainer<>(variant, DirectImageTests.class)
                .withFSync(false)
                .withImagePullPolicy(__ -> false);
    }
}
