package com.github.javister.docker.testing.postgresql;

public class SqlProcessingException extends RuntimeException {
    public SqlProcessingException(String message) {
        super(message);
    }

    public SqlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlProcessingException(Throwable cause) {
        super(cause);
    }
}
