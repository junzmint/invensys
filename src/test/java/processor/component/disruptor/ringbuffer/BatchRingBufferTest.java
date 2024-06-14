package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import processor.component.disruptor.consumer.BatchEventConsumer;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.event.batch.BatchEvent;
import processor.component.disruptor.event.batch.BatchEventFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class BatchRingBufferTest {

    private BatchEventFactory factory;
    private int bufferSize;
    private BatchEventConsumer batchEventConsumer;
    private ClearEventConsumer<BatchEvent> clearEventConsumer;
    private BatchRingBuffer batchRingBuffer;

    @BeforeEach
    void setUp() {
        factory = mock(BatchEventFactory.class);
        bufferSize = 1024;
        batchEventConsumer = mock(BatchEventConsumer.class);
        clearEventConsumer = mock(ClearEventConsumer.class);
        batchRingBuffer = new BatchRingBuffer(factory, bufferSize, batchEventConsumer, clearEventConsumer);
    }

    @Test
    void testGetRingBuffer() {
        try (MockedConstruction<Disruptor> mockedDisruptor = Mockito.mockConstruction(Disruptor.class,
                (disruptor, context) -> {
                    RingBuffer<BatchEvent> mockRingBuffer = mock(RingBuffer.class);
                    EventHandlerGroup<BatchEvent> mockEventHandlerGroup = mock(EventHandlerGroup.class);

                    when(disruptor.getRingBuffer()).thenReturn(mockRingBuffer);
                    when(disruptor.handleEventsWith(batchEventConsumer)).thenReturn(mockEventHandlerGroup);
                    when(mockEventHandlerGroup.then(clearEventConsumer)).thenReturn(null);
                })) {

            RingBuffer<BatchEvent> actualRingBuffer = batchRingBuffer.getRingBuffer();

            // Capture the Disruptor created within getRingBuffer method
            Disruptor<BatchEvent> capturedDisruptor = mockedDisruptor.constructed().get(0);

            verify(capturedDisruptor).handleEventsWith(batchEventConsumer);
            verify(capturedDisruptor.handleEventsWith(batchEventConsumer)).then(clearEventConsumer);
            verify(capturedDisruptor).start();

            assertNotNull(actualRingBuffer);
        }
    }
}
