package processor.component.handler.message.zeromq;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.core.support.subscription.GatewaySubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import processor.component.handler.HandlerLogger;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MessageProduceGatewayTest {

    @Mock
    private GridgoContext mockContext;

    @Mock
    private GatewaySubscription mockGatewaySubscription;

    @Captor
    private ArgumentCaptor<Throwable> exceptionCaptor;

    private MessageProduceGateway messageProduceGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        try (MockedConstruction<DefaultGridgoContextBuilder> mockedConstruction = mockConstruction(DefaultGridgoContextBuilder.class, (mock, context) -> {
            when(mock.setName(anyString())).thenReturn(mock);
            when(mock.setExceptionHandler(any())).thenReturn(mock);
            when(mock.build()).thenReturn(mockContext);
        })) {
            when(mockContext.openGateway(anyString())).thenReturn(mockGatewaySubscription);
            messageProduceGateway = new MessageProduceGateway("replyTo");
        }
    }

    @Test
    void testConstructor() {
        assertNotNull(messageProduceGateway.getContext());
        verify(mockContext).openGateway("replyTo");
        verify(mockGatewaySubscription).attachConnector("zmq:push:replyTo");
        verify(mockContext).start();
    }

    @Test
    void testOnStop() {
        messageProduceGateway.onStop();
        verify(mockContext).stop();
    }

    @Test
    void testOnException() throws Exception {
        Throwable exception = new RuntimeException("Test exception");

        // Access the private method using reflection
        Method onExceptionMethod = MessageProduceGateway.class.getDeclaredMethod("onException", Throwable.class);
        onExceptionMethod.setAccessible(true);

        // Mock the static method before invoking the private method
        try (MockedStatic<HandlerLogger> ignored = mockStatic(HandlerLogger.class)) {
            // Invoke the private method
            onExceptionMethod.invoke(messageProduceGateway, exception);
        }
    }
}
