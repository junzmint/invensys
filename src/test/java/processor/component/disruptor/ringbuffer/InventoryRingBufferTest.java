package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.InventoryEventConsumer;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.disruptor.event.inventory.InventoryEventFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class InventoryRingBufferTest {

    private InventoryEventFactory factory;
    private int bufferSize;
    private InventoryEventConsumer inventoryEventConsumer;
    private ClearEventConsumer<InventoryEvent> clearEventConsumer;
    private InventoryRingBuffer inventoryRingBuffer;

    @BeforeEach
    void setUp() {
        factory = mock(InventoryEventFactory.class);
        bufferSize = 1024;
        inventoryEventConsumer = mock(InventoryEventConsumer.class);
        clearEventConsumer = mock(ClearEventConsumer.class);
        inventoryRingBuffer = new InventoryRingBuffer(factory, bufferSize, inventoryEventConsumer, clearEventConsumer);
    }

    @Test
    void testGetRingBuffer() {
        try (MockedConstruction<Disruptor> mockedDisruptor = Mockito.mockConstruction(Disruptor.class,
                (disruptor, context) -> {
                    RingBuffer<InventoryEvent> mockRingBuffer = mock(RingBuffer.class);
                    EventHandlerGroup<InventoryEvent> mockEventHandlerGroup = mock(EventHandlerGroup.class);

                    when(disruptor.getRingBuffer()).thenReturn(mockRingBuffer);
                    when(disruptor.handleEventsWith(inventoryEventConsumer)).thenReturn(mockEventHandlerGroup);
                    when(mockEventHandlerGroup.then(clearEventConsumer)).thenReturn(null);
                })) {

            RingBuffer<InventoryEvent> actualRingBuffer = inventoryRingBuffer.getRingBuffer();

            // Capture the Disruptor created within getRingBuffer method
            Disruptor<InventoryEvent> capturedDisruptor = mockedDisruptor.constructed().get(0);

            verify(capturedDisruptor).handleEventsWith(inventoryEventConsumer);
            verify(capturedDisruptor.handleEventsWith(inventoryEventConsumer)).then(clearEventConsumer);
            verify(capturedDisruptor).start();

            assertNotNull(actualRingBuffer);
        }
    }
}
