package processor.component.handler.message;

import io.gridgo.core.Gateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.support.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import processor.component.handler.message.zeromq.MessageProduceGateway;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageHandlerTest {

    @Mock
    private MessageProduceGateway mockMessageProduceGateway;

    @Mock
    private GridgoContext mockGridgoContext;

    @Mock
    private GatewaySubscription mockGatewaySubscription;

    @Mock
    private Gateway mockGateway;

    private MessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageHandler = new MessageHandler();
    }

    @Test
    void testSendExistingGateway() throws Exception {
        String replyTo = "replyTo";
        String corrId = "corrId";
        String body = "body";

        when(mockMessageProduceGateway.getContext()).thenReturn(mockGridgoContext);
        when(mockGridgoContext.findGatewayMandatory(replyTo)).thenReturn(mockGateway);
        doNothing().when(mockGateway).send(any(Message.class));

        // Pre-populate the map with the mock gateway
        Map<String, MessageProduceGateway> gatewayMap = new ConcurrentHashMap<>();
        gatewayMap.put(replyTo, mockMessageProduceGateway);
        setMessageProduceGatewayMap(messageHandler, gatewayMap);

        messageHandler.send(replyTo, corrId, body);

        verify(mockGateway, times(1)).send(any(Message.class));
    }

    @Test
    void testSendNewGateway() throws Exception {
        String replyTo = "newReplyTo";
        String corrId = "newCorrId";
        String body = "newBody";

        try (MockedConstruction<MessageProduceGateway> mockedConstruction = mockConstruction(MessageProduceGateway.class, (mock, context) -> {
            when(mock.getContext()).thenReturn(mockGridgoContext);
            when(mockGridgoContext.findGatewayMandatory(replyTo)).thenReturn(mockGateway);
        })) {
            doNothing().when(mockGateway).send(any(Message.class));

            messageHandler.send(replyTo, corrId, body);

            verify(mockGateway, times(1)).send(any(Message.class));
            assertTrue(getMessageProduceGatewayMap(messageHandler).containsKey(replyTo));
        }
    }

    @Test
    void testClose() throws Exception {
        String replyTo = "replyTo";

        when(mockMessageProduceGateway.getContext()).thenReturn(mockGridgoContext);
        when(mockGridgoContext.findGatewayMandatory(replyTo)).thenReturn(mockGateway);

        // Pre-populate the map with the mock gateway
        Map<String, MessageProduceGateway> gatewayMap = new ConcurrentHashMap<>();
        gatewayMap.put(replyTo, mockMessageProduceGateway);
        setMessageProduceGatewayMap(messageHandler, gatewayMap);

        messageHandler.close();

        verify(mockMessageProduceGateway, times(1)).onStop();
        assertTrue(getMessageProduceGatewayMap(messageHandler).isEmpty());
    }

    private void setMessageProduceGatewayMap(MessageHandler handler, Map<String, MessageProduceGateway> map) throws Exception {
        Field field = MessageHandler.class.getDeclaredField("messageProduceGatewayMap");
        field.setAccessible(true);
        field.set(handler, map);
    }

    private Map<String, MessageProduceGateway> getMessageProduceGatewayMap(MessageHandler handler) throws Exception {
        Field field = MessageHandler.class.getDeclaredField("messageProduceGatewayMap");
        field.setAccessible(true);
        return (Map<String, MessageProduceGateway>) field.get(handler);
    }
}
