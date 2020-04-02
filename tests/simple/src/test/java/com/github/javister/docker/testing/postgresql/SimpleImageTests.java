package com.github.javister.docker.testing.postgresql;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class SimpleImageTests {
    @Container
    @SuppressWarnings({"rawtypes", "unchecked", "squid:S1905", "squid:S00117"})
    private static final JavisterPostgreSQLContainer container =
            (JavisterPostgreSQLContainer) new JavisterPostgreSQLContainer(SimpleImageTests.class)
                    .withRelativeFileSystemBind(".", "/app")
                    .withImagePullPolicy(__ -> false);

}