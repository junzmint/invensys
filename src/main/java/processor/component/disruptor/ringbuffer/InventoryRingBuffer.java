package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.InventoryEventConsumer;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.disruptor.event.inventory.InventoryEventFactory;

public class InventoryRingBuffer {
    private final InventoryEventFactory factory;
    private final int bufferSize;
    private final InventoryEventConsumer inventoryEventConsumer;
    private final ClearEventConsumer<InventoryEvent> clearEventConsumer;

    public InventoryRingBuffer(InventoryEventFactory factory, int bufferSize, InventoryEventConsumer inventoryEventConsumer, ClearEventConsumer<InventoryEvent> clearEventConsumer) {
        this.factory = factory;
        this.bufferSize = bufferSize;
        this.inventoryEventConsumer = inventoryEventConsumer;
        this.clearEventConsumer = clearEventConsumer;
    }

    public RingBuffer<InventoryEvent> getRingBuffer() {
        Disruptor<InventoryEvent> disruptor = new Disruptor<>(
                this.factory,
                this.bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());
        
        disruptor.handleEventsWith(this.inventoryEventConsumer).then(this.clearEventConsumer);
        disruptor.start();

        return disruptor.getRingBuffer();
    }
}
