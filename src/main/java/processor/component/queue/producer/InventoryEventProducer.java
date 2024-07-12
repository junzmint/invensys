package processor.component.queue.producer;

import io.gridgo.framework.support.Message;
import processor.component.queue.event.inventory.InventoryEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class InventoryEventProducer {
    private final ArrayBlockingQueue<InventoryEvent> inventoryEventArrayBlockingQueue;

    public InventoryEventProducer(ArrayBlockingQueue<InventoryEvent> queue) {
        this.inventoryEventArrayBlockingQueue = queue;
    }

    public void onData(Long offset, Message message) {
        InventoryEvent inventoryEvent = new InventoryEvent();
        inventoryEvent.setOffSet(offset);
        inventoryEvent.setMessage(message);
        try {
            this.inventoryEventArrayBlockingQueue.put(inventoryEvent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
