package api_gateway.component.http;

import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HttpGatewayLoggerTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        HttpGatewayLogger.setLogger(mockLogger);
    }

    @Test
    void testLogHttpRequest() {
        // Create a mock Message and Deferred
        Message message = Message.ofAny("Test body");
        Deferred<Message, Exception> deferred = new Deferred<Message, Exception>() {
            @Override
            public Deferred<Message, Exception> withTimeout(long l, TimeUnit timeUnit, Supplier<Exception> supplier) {
                return null;
            }

            @Override
            public Deferred<Message, Exception> resolve(Message message) {
                return null;
            }

            @Override
            public Deferred<Message, Exception> reject(Exception exception) {
                return null;
            }

            @Override
            public Promise<Message, Exception> promise() {
                return null;
            }
        };

        // Call the method
        HttpGatewayLogger.logHttpRequest(message, deferred);

        // Capture the log entry
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture(), argCaptor.capture(), argCaptor.capture());

        // Assert the captured values
        assertEquals("POST: {}\nfrom {}", messageCaptor.getValue());
        assertEquals(message.body().toJson(), argCaptor.getAllValues().get(0));
        assertEquals(deferred.toString(), argCaptor.getAllValues().get(1));
        assertMarkerPresent(markerCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("HTTP_REQUEST", marker.getName());
    }
}
