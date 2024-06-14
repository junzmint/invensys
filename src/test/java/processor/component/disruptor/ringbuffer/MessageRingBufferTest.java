package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.MessageEventConsumer;
import processor.component.disruptor.event.message.MessageEvent;
import processor.component.disruptor.event.message.MessageEventFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class MessageRingBufferTest {

    private MessageEventFactory factory;
    private int bufferSize;
    private MessageEventConsumer messageEventConsumer;
    private ClearEventConsumer<MessageEvent> clearEventConsumer;
    private MessageRingBuffer messageRingBuffer;

    @BeforeEach
    void setUp() {
        factory = mock(MessageEventFactory.class);
        bufferSize = 1024;
        messageEventConsumer = mock(MessageEventConsumer.class);
        clearEventConsumer = mock(ClearEventConsumer.class);
        messageRingBuffer = new MessageRingBuffer(factory, bufferSize, messageEventConsumer, clearEventConsumer);
    }

    @Test
    void testGetRingBuffer() {
        try (MockedConstruction<Disruptor> mockedDisruptor = Mockito.mockConstruction(Disruptor.class,
                (disruptor, context) -> {
                    RingBuffer<MessageEvent> mockRingBuffer = mock(RingBuffer.class);
                    EventHandlerGroup<MessageEvent> mockEventHandlerGroup = mock(EventHandlerGroup.class);

                    when(disruptor.getRingBuffer()).thenReturn(mockRingBuffer);
                    when(disruptor.handleEventsWith(messageEventConsumer)).thenReturn(mockEventHandlerGroup);
                    when(mockEventHandlerGroup.then(clearEventConsumer)).thenReturn(null);
                })) {

            RingBuffer<MessageEvent> actualRingBuffer = messageRingBuffer.getRingBuffer();

            // Capture the Disruptor created within getRingBuffer method
            Disruptor<MessageEvent> capturedDisruptor = mockedDisruptor.constructed().get(0);

            verify(capturedDisruptor).handleEventsWith(messageEventConsumer);
            verify(capturedDisruptor.handleEventsWith(messageEventConsumer)).then(clearEventConsumer);
            verify(capturedDisruptor).start();

            assertNotNull(actualRingBuffer);
        }
    }
}
