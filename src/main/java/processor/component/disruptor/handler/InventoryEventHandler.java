package processor.component.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.inventory.InventoryEvent;

public class InventoryEventHandler implements EventHandler<InventoryEvent> {
    @Override
    public void onEvent(InventoryEvent event, long sequence, boolean endOfBatch) {
        System.out.println(event.getOffSet());
    }
}
