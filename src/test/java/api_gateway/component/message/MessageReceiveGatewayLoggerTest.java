package api_gateway.component.message;

import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MessageReceiveGatewayLoggerTest {

    private Logger mockLogger;
    private Deferred<Message, Exception> mockDeferred;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        MessageReceiveGatewayLogger.setLogger(mockLogger); // Add a setter in MessageReceiveGatewayLogger for testing
        mockDeferred = mock(Deferred.class);
    }

    @Test
    void testLogHttpRespond() {
        String message = "Test message";
        MessageReceiveGatewayLogger.logHttpRespond(message, mockDeferred);

        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> deferredCaptor = ArgumentCaptor.forClass(Object.class);

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture(), deferredCaptor.capture(), deferredCaptor.capture());

        assertEquals("Resolved: {} to {}", messageCaptor.getValue());
        assertEquals(message, deferredCaptor.getAllValues().get(0));
        assertEquals(mockDeferred.toString(), deferredCaptor.getAllValues().get(1).toString());
        assertMarkerPresent(markerCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("HTTP_RESPOND", marker.getName());
    }
}
