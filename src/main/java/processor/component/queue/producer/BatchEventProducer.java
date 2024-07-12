package processor.component.queue.producer;

import processor.component.queue.event.batch.BatchEvent;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BatchEventProducer {
    private final ArrayBlockingQueue<BatchEvent> batchEventArrayBlockingQueue;

    public BatchEventProducer(ArrayBlockingQueue<BatchEvent> queue) {
        this.batchEventArrayBlockingQueue = queue;
    }

    public void onData(String type, Long offset, Map<String, Long> batch) {
        BatchEvent batchEvent = new BatchEvent();
        batchEvent.setType(type);
        batchEvent.setOffset(offset);
        batchEvent.setBatch(batch);

        try {
            this.batchEventArrayBlockingQueue.put(batchEvent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
