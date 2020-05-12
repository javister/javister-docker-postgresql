package com.github.javister.docker.testing.postgresql;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;

/**
 * Провайдер контейнеров PostgreSQL по JDBC URL.
 */
public class JavisterPostgreSQLContainerProvider extends JdbcDatabaseContainerProvider {

    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";

    @Override
    public boolean supports(String databaseType) {
        return databaseType.equals(JavisterPostgreSQLContainer.JAVISTER_DRIVER_NAME);
    }

    @Override
    public JdbcDatabaseContainer<?> newInstance() {
        return new JavisterPostgreSQLContainer<>(JavisterPostgreSQLContainer.Variant.V12);
    }

    @Override
    public JdbcDatabaseContainer<?> newInstance(String tag) {
        return new JavisterPostgreSQLContainer<>(JavisterPostgreSQLContainer.Variant.get(tag));
    }

    @Override
    public JdbcDatabaseContainer<?> newInstance(ConnectionUrl connectionUrl) {
        JavisterPostgreSQLContainer<?> jdbcDatabaseContainer =
                (JavisterPostgreSQLContainer<?>) newInstanceFromConnectionUrl(connectionUrl, USER_PARAM, PASSWORD_PARAM);

        String volumePath = connectionUrl.getQueryParameters().get("volumePath");
        if (volumePath != null && !volumePath.isEmpty()) {
            jdbcDatabaseContainer.withVolumePath(volumePath);
        }

        String initScript = connectionUrl.getQueryParameters().get("initScript");
        if (initScript != null && !initScript.isEmpty()) {
            jdbcDatabaseContainer.withInitScript(initScript);
        }

        jdbcDatabaseContainer.withLogConsumer(jdbcDatabaseContainer.getLogConsumer());

        String fsync = connectionUrl.getQueryParameters().getOrDefault("fsync", "on").toLowerCase();
        if (fsync.equals("on") || fsync.equals("true")) {
            jdbcDatabaseContainer.withFSync(true);
        } else if (fsync.equals("off") || fsync.equals("false")) {
            jdbcDatabaseContainer.withFSync(false);
        } else {
            throw new IllegalArgumentException("Parameter fsync of the JDBC connection URL is invalid: " + fsync);
        }

        return jdbcDatabaseContainer;
    }
}
