import com.github.javister.docker.testing.postgresql.ErrorProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorProcessorTest {

    private static final String SCHEMA_PUBLIC_ALREADY_EXISTS_THE_ONLY_WARNING_V11 = "could not execute query:"
            + " ERROR: schema \"public\" already exists Command was: CREATE SCHEMA public;"
            + " WARNING: errors ignored on restore: 1";
    private static final String SCHEMA_PUBLIC_ALREADY_EXISTS_THE_ONLY_WARNING_V12 = "pg_restore: from TOC entry 5; 2615 2200 SCHEMA public postgres\n" +
            "pg_restore: error: could not execute query: ERROR:  schema \"public\" already exists\n" +
            "Command was: CREATE SCHEMA public;\n" +
            "\n" +
            "\n" +
            "\n" +
            "pg_restore: warning: errors ignored on restore: 1";


    @Test
    void v11() {
        assertTrue(ErrorProcessor.canIgnoreError(SCHEMA_PUBLIC_ALREADY_EXISTS_THE_ONLY_WARNING_V11));
    }

    @Test
    void v12() {
        assertTrue(ErrorProcessor.canIgnoreError(SCHEMA_PUBLIC_ALREADY_EXISTS_THE_ONLY_WARNING_V12));
    }

    @Test
    void forcedViaSystemProperty() {
        String oldValue = System.getProperty(ErrorProcessor.IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY);
        System.setProperty(ErrorProcessor.IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY, "true");
        assertTrue(ErrorProcessor.canIgnoreError("some unknown text"));
        if (oldValue != null) {
            System.setProperty(ErrorProcessor.IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY, oldValue);
        } else {
            System.clearProperty(ErrorProcessor.IGNORE_RESTORE_ERRORS_SYSTEM_PROPERTY);
        }
    }
}
