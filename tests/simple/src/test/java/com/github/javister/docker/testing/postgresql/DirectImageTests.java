package com.github.javister.docker.testing.postgresql;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectImageTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectImageTests.class);

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
    void initDbTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException {
        try (JavisterPostgreSQLContainer<?> postgres = getPostgre(variant).withInitScript("SimpleDB.sql")) {
            postgres.start();

            postgres.performQuery(
                    "SELECT foo FROM bar",
                    resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
            );
        }
    }

    @Order(2)
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

    @Order(3)
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

    private JavisterPostgreSQLContainer<?> getPostgre(JavisterPostgreSQLContainer.Variant variant) {
        return new JavisterPostgreSQLContainer<>(variant, DirectImageTests.class)
                .withFSync(false)
                .withImagePullPolicy(__ -> false)
                .withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix("POSTGRE").withRemoveAnsiCodes(false));
    }
}