package processor.component.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.batch.BatchEvent;
import processor.component.handler.batch.BatchHandler;

public class BatchEventConsumer implements EventHandler<BatchEvent> {
    private final BatchHandler batchHandler;

    public BatchEventConsumer(BatchHandler batchHandler) {
        this.batchHandler = batchHandler;
    }

    @Override
    public void onEvent(BatchEvent event, long sequence, boolean endOfBatch) {
        this.batchHandler.handle(event.getType(), event.getOffset(), event.getBatch(), endOfBatch);
    }
}
