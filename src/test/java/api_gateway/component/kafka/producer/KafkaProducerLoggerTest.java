package api_gateway.component.kafka.producer;

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

public class KafkaProducerLoggerTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        KafkaProducerLogger.setLogger(mockLogger);
    }

    @Test
    void testLogKafkaProducerInfoWithMessageAndDeferred() {
        String logMessage = "Test log message";
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

        KafkaProducerLogger.logKafkaProducerInfo(logMessage, deferred);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture(), argCaptor.capture(), argCaptor.capture());

        assertEquals("{} for request from {}", messageCaptor.getValue());
        assertEquals(logMessage, argCaptor.getAllValues().get(0));
        assertEquals(deferred.toString(), argCaptor.getAllValues().get(1));
        assertMarkerPresent(markerCaptor.getValue());
    }

    @Test
    void testLogKafkaProducerInfoWithMessageAckMessageAndDeferred() {
        String logMessage = "Test log message";
        String ackMessage = "Ack message";
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

        KafkaProducerLogger.logKafkaProducerInfo(logMessage, ackMessage, deferred);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Marker> markerCaptor = ArgumentCaptor.forClass(Marker.class);

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture(), argCaptor.capture(), argCaptor.capture(), argCaptor.capture());

        assertEquals("{} for request from {} with ack message {}", messageCaptor.getValue());
        assertEquals(logMessage, argCaptor.getAllValues().get(0));
        assertEquals(deferred.toString(), argCaptor.getAllValues().get(1));
        assertEquals(ackMessage, argCaptor.getAllValues().get(2));
        assertMarkerPresent(markerCaptor.getValue());
    }

    private void assertMarkerPresent(Marker marker) {
        assertNotNull(marker);
        assertEquals("KAFKA_PRODUCER_INFO", marker.getName());
    }
}
