package processor.component.disruptor.event.inventory;

import com.lmax.disruptor.EventFactory;

public class InventoryEventFactory implements EventFactory<InventoryEvent> {
    @Override
    public InventoryEvent newInstance() {
        return new InventoryEvent();
    }
}
