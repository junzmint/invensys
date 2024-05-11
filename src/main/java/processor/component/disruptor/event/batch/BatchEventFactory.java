package processor.component.disruptor.event.batch;

import com.lmax.disruptor.EventFactory;

public class BatchEventFactory implements EventFactory<BatchEvent> {
    @Override
    public BatchEvent newInstance() {
        return new BatchEvent();
    }
}
