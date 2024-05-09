package processor.component.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.inventory.InventoryEvent;

public class InventoryEventConsumer implements EventHandler<InventoryEvent> {
    @Override
    public void onEvent(InventoryEvent event, long sequence, boolean endOfBatch) {
        System.out.println(event.getMessage().body().toString());
    }
}
