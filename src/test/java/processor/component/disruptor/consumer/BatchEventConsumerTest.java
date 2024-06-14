package processor.component.disruptor.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import processor.component.disruptor.event.batch.BatchEvent;
import processor.component.handler.batch.BatchHandler;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BatchEventConsumerTest {

    @Mock
    private BatchHandler mockBatchHandler;

    private BatchEventConsumer batchEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchEventConsumer = new BatchEventConsumer(mockBatchHandler);
    }

    @Test
    void testOnEvent() throws Exception {
        // Create a mock BatchEvent
        BatchEvent mockEvent = mock(BatchEvent.class);
        String eventType = "insert";
        long offset = 12345L;
        Map<String, Long> batch = new HashMap<>();
        batch.put("item1", 10L);

        // Stub the methods of BatchEvent
        when(mockEvent.getType()).thenReturn(eventType);
        when(mockEvent.getOffset()).thenReturn(offset);
        when(mockEvent.getBatch()).thenReturn(batch);

        // Call the onEvent method
        batchEventConsumer.onEvent(mockEvent, 0, true);

        // Verify that the batchHandler's handle method was called with the correct parameters
        verify(mockBatchHandler, times(1)).handle(eq(eventType), eq(offset), eq(batch));
    }

    @Test
    void testOnEventWithNullBatch() throws Exception {
        // Create a mock BatchEvent with a null batch
        BatchEvent mockEvent = mock(BatchEvent.class);
        String eventType = "delete";
        long offset = 54321L;

        // Stub the methods of BatchEvent
        when(mockEvent.getType()).thenReturn(eventType);
        when(mockEvent.getOffset()).thenReturn(offset);
        when(mockEvent.getBatch()).thenReturn(null);

        // Call the onEvent method
        batchEventConsumer.onEvent(mockEvent, 0, true);

        // Verify that the batchHandler's handle method was called with the correct parameters
        verify(mockBatchHandler, times(1)).handle(eq(eventType), eq(offset), eq(null));
    }
}

