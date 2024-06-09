package api_gateway.component;

import api_gateway.component.http.HttpGateway;
import api_gateway.component.kafka.producer.KafkaProducer;
import api_gateway.component.message.MessageReceiveGateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.core.support.ContextAwareComponent;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ApiGatewayTest {

    private ApiGateway apiGateway;
    private GridgoContext mockAppContext;
    private KafkaProducer mockKafkaProducer;
    private HttpGateway mockHttpGateway;
    private Map<String, Deferred<Message, Exception>> mockDeferredMap;
    private MessageReceiveGateway mockMessageReceiveGateway;
    private GatewaySubscription mockHttpGatewaySubscription;
    private GatewaySubscription mockZmqGatewaySubscription;

    @BeforeEach
    void setUp() {
        // Mock the components
        mockAppContext = mock(GridgoContext.class);
        mockKafkaProducer = mock(KafkaProducer.class);
        mockHttpGateway = mock(HttpGateway.class);
        mockDeferredMap = mock(ConcurrentHashMap.class);
        mockMessageReceiveGateway = mock(MessageReceiveGateway.class);
        mockHttpGatewaySubscription = mock(GatewaySubscription.class);
        mockZmqGatewaySubscription = mock(GatewaySubscription.class);

        // Mock the behavior of openGateway and attachConnector
        when(mockAppContext.openGateway(ApiGatewayConstants.getHttpGateway())).thenReturn(mockHttpGatewaySubscription);
        when(mockAppContext.openGateway(ApiGatewayConstants.getZMQPullGateway())).thenReturn(mockZmqGatewaySubscription);
        when(mockHttpGatewaySubscription.attachConnector(ApiGatewayConstants.getVertxUrl())).thenReturn(mockHttpGatewaySubscription);
        when(mockZmqGatewaySubscription.attachConnector("zmq:pull:tcp://0.0.0.0:5555")).thenReturn(mockZmqGatewaySubscription);

        // Initialize the ApiGateway
        try (var mocked = mockConstruction(DefaultGridgoContextBuilder.class, (mock, context) -> {
            when(mock.setName(anyString())).thenReturn(mock);
            when(mock.setExceptionHandler(any())).thenReturn(mock);
            when(mock.build()).thenReturn(mockAppContext);
        })) {
            apiGateway = new ApiGateway();
        }

        // Inject the mocks using reflection
        try {
            var fieldAppContext = ApiGateway.class.getDeclaredField("appContext");
            fieldAppContext.setAccessible(true);
            fieldAppContext.set(apiGateway, mockAppContext);

            var fieldKafkaProducer = ApiGateway.class.getDeclaredField("kafkaProducer");
            fieldKafkaProducer.setAccessible(true);
            fieldKafkaProducer.set(apiGateway, mockKafkaProducer);

            var fieldHttpGateway = ApiGateway.class.getDeclaredField("httpGateway");
            fieldHttpGateway.setAccessible(true);
            fieldHttpGateway.set(apiGateway, mockHttpGateway);

            var fieldDeferredMap = ApiGateway.class.getDeclaredField("deferredMap");
            fieldDeferredMap.setAccessible(true);
            fieldDeferredMap.set(apiGateway, mockDeferredMap);

            var fieldMessageReceiveGateway = ApiGateway.class.getDeclaredField("messageReceiveGateway");
            fieldMessageReceiveGateway.setAccessible(true);
            fieldMessageReceiveGateway.set(apiGateway, mockMessageReceiveGateway);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructor() {
        // Verify the order of method calls
        InOrder inOrder = inOrder(mockAppContext, mockHttpGatewaySubscription, mockZmqGatewaySubscription);
        inOrder.verify(mockAppContext).openGateway(ApiGatewayConstants.getHttpGateway());
        inOrder.verify(mockHttpGatewaySubscription).attachConnector(ApiGatewayConstants.getVertxUrl());
        inOrder.verify(mockAppContext).openGateway(ApiGatewayConstants.getZMQPullGateway());
        inOrder.verify(mockZmqGatewaySubscription).attachConnector("zmq:pull:tcp://0.0.0.0:5555");

        // Check if HttpGateway and MessageReceiveGateway instances are attached to AppContext
        // Instance capture the params (the instances) will be attached to AppContext
        ArgumentCaptor<ContextAwareComponent> argumentCaptor = ArgumentCaptor.forClass(ContextAwareComponent.class);
        // Check attachComponent is called 2 times with 2 instances and capture the instances
        verify(mockAppContext, times(2)).attachComponent(argumentCaptor.capture());

        // Get instances captured
        var capturedValues = argumentCaptor.getAllValues();
        // Check captured instances
        assertTrue(capturedValues.stream().anyMatch(component -> component.getClass().equals(HttpGateway.class)));
        assertTrue(capturedValues.stream().anyMatch(component -> component.getClass().equals(MessageReceiveGateway.class)));
    }

    @Test
    void testOnStart() {
        apiGateway.onStart();
        verify(mockAppContext).start();
    }

    @Test
    void testOnStop() {
        apiGateway.onStop();

        verify(mockKafkaProducer).onClose();
        verify(mockHttpGateway).stop();
        verify(mockMessageReceiveGateway).stop();
        verify(mockDeferredMap).clear();
        verify(mockAppContext).stop();
    }

    @Test
    void testOnException() throws Exception {
        Throwable testException = new RuntimeException("Test exception");

        Method method = ApiGateway.class.getDeclaredMethod("onException", Throwable.class);
        method.setAccessible(true);

        // Use MockedStatic to mock the static method
        try (MockedStatic<ApiGatewayLogger> mockedStaticLogger = Mockito.mockStatic(ApiGatewayLogger.class)) {
            method.invoke(apiGateway, testException);
            mockedStaticLogger.verify(() -> ApiGatewayLogger.logApiGatewayError("INTERNAL_ERROR", testException));
        }
    }
}
