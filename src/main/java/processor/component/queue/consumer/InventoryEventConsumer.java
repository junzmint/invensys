package processor.component.queue.consumer;

import processor.component.handler.inventory.InventoryHandler;
import processor.component.queue.event.inventory.InventoryEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class InventoryEventConsumer implements Runnable {
    private final ArrayBlockingQueue<InventoryEvent> inventoryEventArrayBlockingQueue;
    private final InventoryHandler inventoryHandler;

    public InventoryEventConsumer(ArrayBlockingQueue<InventoryEvent> queue, InventoryHandler inventoryHandler) {
        this.inventoryEventArrayBlockingQueue = queue;
        this.inventoryHandler = inventoryHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                InventoryEvent inventoryEvent = inventoryEventArrayBlockingQueue.take();
                this.inventoryHandler.handle(inventoryEvent.getMessage(), inventoryEvent.getOffSet(), true);
                inventoryEvent.clear();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
