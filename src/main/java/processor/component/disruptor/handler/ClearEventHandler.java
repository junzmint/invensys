package processor.component.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.Clearable;

public class ClearEventHandler<T> implements EventHandler<T> {
    @Override
    public void onEvent(T event, long sequence, boolean endOfBatch) {
        if (event instanceof Clearable) {
            ((Clearable) event).clear();
        }
    }
}
