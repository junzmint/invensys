package api_gateway.component.http;

import api_gateway.component.kafka.producer.KafkaProducer;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HttpGatewayTest {

    private KafkaProducer mockKafkaProducer;
    private Map<String, Deferred<Message, Exception>> deferredMap;
    private HttpGateway httpGateway;
    private RoutingContext mockRoutingContext;
    private GridgoContext mockGridgoContext;
    private GatewaySubscription mockSubscription;
    private Deferred<Message, Exception> mockDeferred;
    private Message mockMessage;
    private BObject mockHeaders;

    @BeforeEach
    void setUp() {
        mockKafkaProducer = mock(KafkaProducer.class);
        deferredMap = new HashMap<>();
        httpGateway = new HttpGateway("testGateway", mockKafkaProducer, deferredMap, "replyToAddress");
        mockRoutingContext = mock(RoutingContext.class);
        mockGridgoContext = mock(GridgoContext.class);
        mockSubscription = mock(GatewaySubscription.class);
        mockDeferred = mock(Deferred.class);
        mockMessage = mock(Message.class);
        mockHeaders = mock(BObject.class);

        httpGateway.setContext(mockGridgoContext);

        when(mockRoutingContext.getMessage()).thenReturn(mockMessage);
        when(mockRoutingContext.getDeferred()).thenReturn(mockDeferred);
        when(mockMessage.headers()).thenReturn(mockHeaders);
    }

    @Test
    void testProcessRequestWithNullBody() {
        when(mockMessage.body()).thenReturn(null);

        AtomicLong corrId = httpGateway.getCorrId();
        long initialId = corrId.get();

        httpGateway.processRequest(mockRoutingContext, mockGridgoContext);

        assertEquals(initialId + 1, corrId.get());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Deferred<Message, Exception>> deferredCaptor = ArgumentCaptor.forClass(Deferred.class);

        verify(mockKafkaProducer).produce(messageCaptor.capture(), deferredCaptor.capture(), keyCaptor.capture(), eq(false));
        verify(mockHeaders).setAny(eq("corrId"), keyCaptor.capture());
        verify(mockHeaders).setAny(eq("replyTo"), eq("replyToAddress"));
        assertEquals(deferredMap.get(keyCaptor.getValue()), mockDeferred);

        HttpGatewayLogger.logHttpRequest(mockMessage, mockDeferred);
    }

    @Test
    void testProcessRequestWithNonNullBody() {
        when(mockMessage.body()).thenReturn(BValue.of("testBody"));

        AtomicLong corrId = httpGateway.getCorrId();
        long initialId = corrId.get();

        httpGateway.processRequest(mockRoutingContext, mockGridgoContext);

        assertEquals(initialId + 1, corrId.get());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Deferred<Message, Exception>> deferredCaptor = ArgumentCaptor.forClass(Deferred.class);

        verify(mockKafkaProducer).produce(messageCaptor.capture(), deferredCaptor.capture(), keyCaptor.capture(), eq(false));
        verify(mockHeaders).setAny(eq("corrId"), keyCaptor.capture());
        verify(mockHeaders).setAny(eq("replyTo"), eq("replyToAddress"));
        assertEquals(deferredMap.get(keyCaptor.getValue()), mockDeferred);

        HttpGatewayLogger.logHttpRequest(mockMessage, mockDeferred);
    }

    @Test
    void testOnStartWithGatewayPresent() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(Optional.of(mockSubscription));

        httpGateway.onStart();

        verify(mockSubscription).subscribe(any());
    }

    @Test
    void testOnStartWithGatewayAbsent() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(Optional.empty());

        httpGateway.onStart();

        verify(mockSubscription, never()).subscribe(any());
    }

    @Test
    void testOnStop() {
        httpGateway.onStop();

        verify(mockGridgoContext).stop();
    }

    @Test
    void testGetGateway() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(Optional.of(mockSubscription));

        Optional<GatewaySubscription> result = httpGateway.getGateway();

        assertTrue(result.isPresent());
        assertEquals(mockSubscription, result.get());
    }

    @Test
    void testGetGatewayWithEmpty() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(Optional.empty());

        Optional<GatewaySubscription> result = httpGateway.getGateway();

        assertFalse(result.isPresent());
    }
}
