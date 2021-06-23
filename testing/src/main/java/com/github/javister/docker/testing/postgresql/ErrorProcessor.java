package com.github.javister.docker.testing.postgresql;

public class ErrorProcessor {
    public static final String IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY = "com.github.javister.docker.testing.postgresql.ignore.restore.errors";
    private static final String SCHEMA_PUBLIC_ALREADY_EXISTS = "schema \"public\" already exists";
    private static final String ERRORS_IGNORED_ON_RESTORE_1 = "errors ignored on restore: 1";

    public static boolean canIgnoreError(String errorText) {
        if (errorText == null) {
            return false;
        }
        String preparedText = errorText.replace("\n\n", "\n").replaceAll("\\s+", " ");
        if (preparedText.contains(SCHEMA_PUBLIC_ALREADY_EXISTS) && preparedText.contains(ERRORS_IGNORED_ON_RESTORE_1)) {
            return true;
        }
        return Boolean.getBoolean(IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY);
    }
}
