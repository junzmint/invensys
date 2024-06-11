package processor.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProcessorLoggerTest {

    private Logger mockLogger;
    private ProcessorLogger logger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        ProcessorLogger.setLogger(mockLogger); // Add a setter in ApiGatewayLogger for testing
    }

    @Test
    void testLogApiGatewayErrorWithThrowable() {
        Throwable throwable = new RuntimeException("Test exception");
        ProcessorLogger.logProcessorError("CONFIG_LOADER_ERROR", throwable);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);

        verify(mockLogger).error(markerCaptor.capture(), messageCaptor.capture(), throwableCaptor.capture());

        assertEquals("CONFIG_LOADER_ERROR", messageCaptor.getValue());
        assertEquals(throwable, throwableCaptor.getValue());
        assertMarkerPresent(markerCaptor.getValue());
    }

    @Test
    void testLogApiGatewayErrorWithException() {
        Exception exception = new Exception("Test exception");
        ProcessorLogger.logProcessorError("CONFIG_LOADER_ERROR", exception);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);

        verify(mockLogger).error(markerCaptor.capture(), messageCaptor.capture(), throwableCaptor.capture());

        assertEquals("CONFIG_LOADER_ERROR", messageCaptor.getValue());
        assertEquals(exception, throwableCaptor.getValue());
        assertMarkerPresent(markerCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("PROCESSOR_ERROR", marker.getName());
    }
}
