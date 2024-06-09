package api_gateway.component.message;

import io.gridgo.bean.BObject;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MessageReceiveGatewayTest {

    private MessageReceiveGateway messageReceiveGateway;
    private GridgoContext mockGridgoContext;
    private RoutingContext mockRoutingContext;
    private Deferred<Message, Exception> mockDeferred;
    private GatewaySubscription mockSubscription;
    private Message mockMessage;
    private Map<String, Deferred<Message, Exception>> deferredMap;

    @BeforeEach
    void setUp() {
        deferredMap = new HashMap<>();
        messageReceiveGateway = new MessageReceiveGateway("testGateway", deferredMap);

        mockGridgoContext = mock(GridgoContext.class);
        mockRoutingContext = mock(RoutingContext.class);
        mockSubscription = mock(GatewaySubscription.class);
        mockDeferred = mock(Deferred.class);
        mockMessage = mock(Message.class);

        messageReceiveGateway.setContext(mockGridgoContext);

        when(mockRoutingContext.getMessage()).thenReturn(mockMessage);
    }

    @Test
    void testProcessRequestWithNullDeferred() {
        when(mockMessage.headers()).thenReturn(io.gridgo.bean.BObject.ofEmpty());

        messageReceiveGateway.processRequest(mockRoutingContext, mockGridgoContext);

        verify(mockDeferred, never()).resolve(any());
        verify(mockDeferred, never()).reject(any());
        // Verify log
        // Add verification for logging if required
    }

    @Test
    void testProcessRequestWithValidDeferred() {
        io.gridgo.bean.BObject headers = BObject.ofEmpty().setAny("corrId", "testId");
        when(mockMessage.headers()).thenReturn(headers);
        when(mockMessage.body()).thenReturn(BObject.ofEmpty());
        deferredMap.put("testId", mockDeferred);

        messageReceiveGateway.processRequest(mockRoutingContext, mockGridgoContext);

        verify(mockDeferred).resolve(any(Message.class));
        verify(mockDeferred, never()).reject(any());

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockDeferred).resolve(messageCaptor.capture());

        assertEquals(BObject.ofEmpty(), messageCaptor.getValue().body());
    }

    @Test
    void testProcessRequestWithException() {
        io.gridgo.bean.BObject headers = BObject.ofEmpty().setAny("corrId", "testId");
        when(mockMessage.headers()).thenReturn(headers);
        when(mockMessage.body()).thenThrow(new RuntimeException("Test exception"));
        deferredMap.put("testId", mockDeferred);

        messageReceiveGateway.processRequest(mockRoutingContext, mockGridgoContext);

        verify(mockDeferred, never()).resolve(any());
        verify(mockDeferred).reject(any(Exception.class));
    }

    @Test
    void testOnStartWithGatewayPresent() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(java.util.Optional.of(mockSubscription));

        messageReceiveGateway.onStart();

        verify(mockSubscription).subscribe(any());
    }

    @Test
    void testOnStartWithGatewayAbsent() {
        when(mockGridgoContext.getGatewaySubscription("testGateway")).thenReturn(Optional.empty());

        messageReceiveGateway.onStart();

        verify(mockSubscription, never()).subscribe(any());
    }

    @Test
    void testOnStop() {
        messageReceiveGateway.onStop();

        verify(mockGridgoContext).stop();
    }
}
