package processor.component.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalCacheLoggerTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        LocalCacheLogger.setLogger(mockLogger);
    }

    @Test
    void testLogCacheStat() {
        String message = "Test cache stat message";

        LocalCacheLogger.logCacheStat(message);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockLogger).info(markerCaptor.capture(), stringCaptor.capture());

        assertMarkerPresent(markerCaptor.getValue());
        assertEquals(message, stringCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("CACHE_STAT", marker.getName());
    }
}
