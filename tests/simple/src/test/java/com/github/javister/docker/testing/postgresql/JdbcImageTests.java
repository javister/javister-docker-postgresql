package com.github.javister.docker.testing.postgresql;

import com.github.javister.docker.testing.base.JavisterBaseContainer;
import org.junit.AfterClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import java.io.IOException;
import java.sql.SQLException;

import static com.github.javister.docker.testing.postgresql.JavisterPostgreSQLContainer.JAVISTER_DRIVER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcImageTests {
    private static final String JDBC_URL_TEMPLATE = "jdbc:tc:%s:%s:///system?password=somepwd&fsync=off&volumePath=%s&initScript=%s";

    @AfterClass
    public static void testCleanup() {
        ContainerDatabaseDriver.killContainers();
    }

    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void simpleTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException {
        JavisterPostgreSQLContainer.performQuery(
                String.format(JDBC_URL_TEMPLATE, JAVISTER_DRIVER_NAME, variant.getValue(), "", ""),
                "SELECT 1",
                resultSet -> assertEquals(1, resultSet.getInt(1), "A basic SELECT query succeeds")
        );
    }

    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void simpleWithVolumeTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException {
        String volumePath = JavisterBaseContainer.getTestPath(JdbcImageTests.class) + "/simpleWithVolumeTest-" + variant.getValue();
        JavisterBaseContainer.deleteDir(volumePath);
        JavisterPostgreSQLContainer.performQuery(
                String.format(JDBC_URL_TEMPLATE, JAVISTER_DRIVER_NAME, variant.getValue(), volumePath, ""),
                "SELECT 1",
                resultSet -> assertEquals(1, resultSet.getInt(1), "A basic SELECT query succeeds")
        );
    }

    @ParameterizedTest(name = "PostgreSQL variant: {0}")
    @EnumSource(JavisterPostgreSQLContainer.Variant.class)
    void initDbTest(JavisterPostgreSQLContainer.Variant variant) throws SQLException, IOException {
        String volumePath = JavisterBaseContainer.getTestPath(JdbcImageTests.class) + "/initDbTest-" + variant.getValue();
        JavisterBaseContainer.deleteDir(volumePath);
        JavisterPostgreSQLContainer.performQuery(
                String.format(JDBC_URL_TEMPLATE, JAVISTER_DRIVER_NAME, variant.getValue(), volumePath, "SimpleDB.sql"),
                "SELECT foo FROM bar",
                resultSet -> assertEquals("hello world", resultSet.getString(1), "Value from init script should equal real value")
        );
    }
}
