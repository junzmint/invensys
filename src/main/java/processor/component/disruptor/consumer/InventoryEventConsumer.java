package processor.component.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.inventory.InventoryEvent;
import processor.component.handler.inventory.InventoryHandler;

public class InventoryEventConsumer implements EventHandler<InventoryEvent> {
    private final InventoryHandler inventoryHandler;

    public InventoryEventConsumer(InventoryHandler inventoryHandler) {
        this.inventoryHandler = inventoryHandler;
    }

    @Override
    public void onEvent(InventoryEvent event, long sequence, boolean endOfBatch) {
        inventoryHandler.handle(event.getMessage());
    }
}
