package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.InventoryEventConsumer;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.disruptor.event.inventory.InventoryEventFactory;

public class InventoryRingBuffer {
    private final InventoryEventFactory factory;
    private final int bufferSize;
    private final InventoryEventConsumer inventoryEventHandler;
    private final ClearEventConsumer<InventoryEvent> clearEventHandler;

    public InventoryRingBuffer(InventoryEventFactory factory, int bufferSize, InventoryEventConsumer inventoryEventHandler, ClearEventConsumer<InventoryEvent> clearEventHandler) {
        this.factory = factory;
        this.bufferSize = bufferSize;
        this.inventoryEventHandler = inventoryEventHandler;
        this.clearEventHandler = clearEventHandler;
    }

    public RingBuffer<InventoryEvent> getRingBuffer() {
        Disruptor<InventoryEvent> disruptor =
                new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(inventoryEventHandler).then(clearEventHandler);
        disruptor.start();

        return disruptor.getRingBuffer();
    }
}
