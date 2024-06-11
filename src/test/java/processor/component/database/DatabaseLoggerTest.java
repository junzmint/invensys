package processor.component.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseLoggerTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        DatabaseLogger.setLogger(mockLogger);
    }

    @Test
    void testLogDatabaseInfo() {
        String message = "Test info message";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        DatabaseLogger.logDatabaseInfo(message, stackTrace);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);

        verify(mockLogger).info(markerCaptor.capture(), stringCaptor.capture(), argCaptor.capture(), argCaptor.capture());

        assertEquals("in {}: {}", stringCaptor.getValue());
        assertEquals(stackTrace[1], argCaptor.getAllValues().get(0));
        assertEquals(message, argCaptor.getAllValues().get(1));
        assertMarkerPresent("DATABASE_INFO", markerCaptor.getValue());
    }

    @Test
    void testLogDatabaseError() {
        String message = "Test error message";
        Exception exception = new Exception("Test exception");

        DatabaseLogger.logDatabaseError(message, exception);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(mockLogger).error(markerCaptor.capture(), stringCaptor.capture(), exceptionCaptor.capture());

        assertMarkerPresent("DATABASE_ERROR", markerCaptor.getValue());
        assertEquals(message, stringCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }

    private void assertMarkerPresent(String markerString, Marker marker) {
        assertNotNull(marker);
        assertEquals(markerString, marker.getName());
    }
}
