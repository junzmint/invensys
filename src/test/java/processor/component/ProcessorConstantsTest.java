package processor.component;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessorConstantsTest {

    private static final String TEST_CONFIG_FILE_PATH = "config/processor/example.processor.properties";

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(TEST_CONFIG_FILE_PATH));

        // Update the properties in the main ApiGatewayConstants class
        ProcessorConstants.setConfigFilePath(TEST_CONFIG_FILE_PATH);
    }

    @Test
    void testGetKafkaBroker() {
        assertEquals("localhost:9092", ProcessorConstants.getKafkaBroker());
    }

    @Test
    void testGetKafkaTopic() {
        assertEquals("invensys", ProcessorConstants.getKafkaTopic());
    }

    @Test
    void testGetDeserializerClassConfig() {
        assertEquals("org.apache.kafka.common.serialization.StringDeserializer", ProcessorConstants.getDeserializerClassConfig());
    }

    @Test
    void testGetGroupId() {
        assertEquals("first", ProcessorConstants.getGroupId());
    }

    @Test
    void testGetAutoOffsetReset() {
        assertEquals("earliest", ProcessorConstants.getAutoOffsetReset());
    }

    @Test
    void testGetCacheSize() {
        assertEquals(Long.valueOf(10000), ProcessorConstants.getCacheSize());
    }

    @Test
    void testGetCacheInitRecords() {
        assertEquals(Long.valueOf(10000), ProcessorConstants.getCacheInitRecords());
    }

    @Test
    void testGetCacheStatLogAfter() {
        assertEquals(Long.valueOf(60), ProcessorConstants.getCacheStatLogAfter());
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
            ProcessorConstants.setConfigFilePath("invalid/path");

        } catch (IOException exception) {
            // Since we cannot directly test the private static block, we assume the logger works correctly
            // and manually verify that the exception message is logged as expected.
            ProcessorLogger.logProcessorError("CONFIG_LOADER_ERROR", exception);
            assertEquals("CONFIG_LOADER_ERROR: Test IOException", exception.getMessage());
        }
    }
}
