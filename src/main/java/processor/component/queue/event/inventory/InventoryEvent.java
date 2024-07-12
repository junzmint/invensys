package processor.component.queue.event.inventory;

import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.Setter;
import processor.component.queue.event.Clearable;

@Setter
@Getter
public class InventoryEvent implements Clearable {
    private Long offSet;
    private Message message;

    @Override
    public void clear() {
        this.offSet = null;
        this.message = null;
    }
}
