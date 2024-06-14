package processor.component.disruptor.consumer;

import io.gridgo.framework.support.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.handler.inventory.InventoryHandler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InventoryEventConsumerTest {

    @Mock
    private InventoryHandler mockInventoryHandler;

    private InventoryEventConsumer inventoryEventConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryEventConsumer = new InventoryEventConsumer(mockInventoryHandler);
    }

    @Test
    void testOnEvent() throws Exception {
        // Create a mock InventoryEvent
        InventoryEvent mockEvent = mock(InventoryEvent.class);
        long offset = 12345L;
        Message message = Message.ofEmpty();

        // Stub the methods of InventoryEvent
        when(mockEvent.getOffSet()).thenReturn(offset);
        when(mockEvent.getMessage()).thenReturn(message);

        // Call the onEvent method
        inventoryEventConsumer.onEvent(mockEvent, 0, true);

        // Verify that the inventoryHandler's handle method was called with the correct parameters
        verify(mockInventoryHandler, times(1)).handle(eq(message), eq(offset), eq(true));
    }

    @Test
    void testOnEventWithEndOfBatchFalse() throws Exception {
        // Create a mock InventoryEvent
        InventoryEvent mockEvent = mock(InventoryEvent.class);
        long offset = 54321L;
        Message message = Message.ofEmpty();

        // Stub the methods of InventoryEvent
        when(mockEvent.getOffSet()).thenReturn(offset);
        when(mockEvent.getMessage()).thenReturn(message);

        // Call the onEvent method with endOfBatch set to false
        inventoryEventConsumer.onEvent(mockEvent, 0, false);

        // Verify that the inventoryHandler's handle method was called with the correct parameters
        verify(mockInventoryHandler, times(1)).handle(eq(message), eq(offset), eq(false));
    }
}

