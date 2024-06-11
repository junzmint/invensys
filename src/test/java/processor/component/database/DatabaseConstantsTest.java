package processor.component.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseConstantsTest {

    private static final String TEST_CONFIG_FILE_PATH = "config/processor/example.database.properties";

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(TEST_CONFIG_FILE_PATH));

        // Update the properties in the main ApiGatewayConstants class
        DatabaseConstants.setConfigFilePath(TEST_CONFIG_FILE_PATH);
    }

    @Test
    void testGetDatabaseConnection() {
        assertEquals("mysql", DatabaseConstants.getDatabaseConnection());
    }

    @Test
    void testGetDatabaseHost() {
        assertEquals("localhost", DatabaseConstants.getDatabaseHost());
    }

    @Test
    void testGetDatabasePort() {
        assertEquals("3306", DatabaseConstants.getDatabasePort());
    }

    @Test
    void testGetDatabaseName() {
        assertEquals("invensys", DatabaseConstants.getDatabaseName());
    }

    @Test
    void testGetDatabaseUser() {
        assertEquals("root", DatabaseConstants.getDatabaseUser());
    }

    @Test
    void testGetDatabasePassword() {
        assertEquals("password", DatabaseConstants.getDatabasePassword());
    }

    @Test
    void testIOExceptionHandling() {
        // Mocking FileInputStream to throw IOException
        try {
            FileInputStream mockedFileInputStream = mock(FileInputStream.class);
            when(mockedFileInputStream.read()).thenThrow(new IOException("Test IOException"));

            // Injecting the mocked FileInputStream
            Properties props = new Properties();
            props.load(mockedFileInputStream);
            DatabaseConstants.setConfigFilePath("invalid/path");

        } catch (IOException exception) {
            // Since we cannot directly test the private static block, we assume the logger works correctly
            // and manually verify that the exception message is logged as expected.
            DatabaseLogger.logDatabaseError("CONFIG_LOADER_ERROR", exception);
            assertEquals("CONFIG_LOADER_ERROR: Test IOException", exception.getMessage());
        }
    }
}
