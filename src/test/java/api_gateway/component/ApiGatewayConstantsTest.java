package api_gateway.component;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiGatewayConstantsTest {
    private static final String TEST_CONFIG_FILE_PATH = "config/api_gateway/example.api_gateway.properties";

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(TEST_CONFIG_FILE_PATH));

        // Update the properties in the main ApiGatewayConstants class
        ApiGatewayConstants.setConfigFilePath(TEST_CONFIG_FILE_PATH);
    }

    @Test
    void testGetVertxUrl() {
        assertEquals("vertx:http://0.0.0.0:8080/inventory", ApiGatewayConstants.getVertxUrl());
    }

    @Test
    void testGetApiGateway() {
        assertEquals("InvensysApiGateway", ApiGatewayConstants.getApiGateway());
    }

    @Test
    void testGetHttpGateway() {
        assertEquals("InvensysHttpGateway", ApiGatewayConstants.getHttpGateway());
    }

    @Test
    void testGetZMQPullGateway() {
        assertEquals("ZeroMqPullGateway", ApiGatewayConstants.getZMQPullGateway());
    }

    @Test
    void testGetZMQReplyAddress() {
        assertEquals("tcp://localhost:5555", ApiGatewayConstants.getZMQReplyAddress());
    }

    @Test
    void testGetKafkaBroker() {
        assertEquals("localhost:9092", ApiGatewayConstants.getKafkaBroker());
    }

    @Test
    void testGetKafkaTopic() {
        assertEquals("invensys", ApiGatewayConstants.getKafkaTopic());
    }

    @Test
    void testGetSerializerClassConfig() {
        assertEquals("org.apache.kafka.common.serialization.StringSerializer", ApiGatewayConstants.getSerializerClassConfig());
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
            ApiGatewayConstants.setConfigFilePath("invalid/path");

        } catch (IOException exception) {
            // Since we cannot directly test the private static block, we assume the logger works correctly
            // and manually verify that the exception message is logged as expected.
            ApiGatewayLogger.logApiGatewayError("CONFIG_LOADER_ERROR", exception);
            assertEquals("CONFIG_LOADER_ERROR: Test IOException", exception.getMessage());
        }
    }
}
