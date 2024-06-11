package processor.component.kafka.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KafkaConsumerLoggerTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        KafkaConsumerLogger.setLogger(mockLogger);
    }

    @Test
    void testLogKafkaConsumerInfo() {
        String message = "Test Kafka consumer info message";

        KafkaConsumerLogger.logKafkaConsumerInfo(message);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockLogger).info(markerCaptor.capture(), stringCaptor.capture());

        assertMarkerPresent(markerCaptor.getValue());
        assertEquals(message, stringCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("KAFKA_CONSUMER_INFO", marker.getName());
    }
}
