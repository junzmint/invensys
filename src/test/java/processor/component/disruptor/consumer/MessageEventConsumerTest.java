package processor.component.disruptor.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import processor.component.disruptor.event.message.MessageEvent;
import processor.component.handler.message.MessageHandler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessageEventConsumerTest {

    @Mock
    private MessageHandler mockMessageHandler;

    private MessageEventConsumer messageEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageEventConsumer = new MessageEventConsumer(mockMessageHandler);
    }

    @Test
    void testOnEvent() throws Exception {
        // Create a mock MessageEvent
        MessageEvent mockEvent = mock(MessageEvent.class);
        String replyTo = "replyTo";
        String corrId = "corrId";
        String body = "body";

        // Stub the methods of MessageEvent
        when(mockEvent.getReplyTo()).thenReturn(replyTo);
        when(mockEvent.getCorrId()).thenReturn(corrId);
        when(mockEvent.getBody()).thenReturn(body);

        // Call the onEvent method
        messageEventConsumer.onEvent(mockEvent, 0, true);

        // Verify that the messageHandler's send method was called with the correct parameters
        verify(mockMessageHandler, times(1)).send(eq(replyTo), eq(corrId), eq(body));
    }

    @Test
    void testOnEventWithEndOfBatchFalse() throws Exception {
        // Create a mock MessageEvent
        MessageEvent mockEvent = mock(MessageEvent.class);
        String replyTo = "replyTo";
        String corrId = "corrId";
        String body = "body";

        // Stub the methods of MessageEvent
        when(mockEvent.getReplyTo()).thenReturn(replyTo);
        when(mockEvent.getCorrId()).thenReturn(corrId);
        when(mockEvent.getBody()).thenReturn(body);

        // Call the onEvent method with endOfBatch set to false
        messageEventConsumer.onEvent(mockEvent, 0, false);

        // Verify that the messageHandler's send method was called with the correct parameters
        verify(mockMessageHandler, times(1)).send(eq(replyTo), eq(corrId), eq(body));
    }
}
