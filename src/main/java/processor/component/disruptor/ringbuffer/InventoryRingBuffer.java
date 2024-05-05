package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.disruptor.event.inventory.InventoryEventFactory;
import processor.component.disruptor.handler.ClearEventHandler;
import processor.component.disruptor.handler.InventoryEventHandler;

public class InventoryRingBuffer {
    private final InventoryEventFactory factory;
    private final int bufferSize;
    private final InventoryEventHandler inventoryEventHandler;
    private final ClearEventHandler<InventoryEvent> clearEventHandler;

    public InventoryRingBuffer(InventoryEventFactory factory, int bufferSize, InventoryEventHandler inventoryEventHandler, ClearEventHandler<InventoryEvent> clearEventHandler) {
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
