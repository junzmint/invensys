package processor.component.queue.consumer;

import processor.component.handler.batch.BatchHandler;
import processor.component.queue.event.batch.BatchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class BatchEventConsumer implements Runnable {
    private final ArrayBlockingQueue<BatchEvent> batchEventArrayBlockingQueue;
    private final BatchHandler batchHandler;

    public BatchEventConsumer(ArrayBlockingQueue<BatchEvent> queue, BatchHandler batchHandler) {
        this.batchEventArrayBlockingQueue = queue;
        this.batchHandler = batchHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
//                BatchEvent batchEvent = batchEventArrayBlockingQueue.take();
//                this.batchHandler.handle(batchEvent.getType(), batchEvent.getOffset(), batchEvent.getBatch());
//                batchEvent.clear();

                List<BatchEvent> batchEventList = new ArrayList<>();
                batchEventArrayBlockingQueue.drainTo(batchEventList);

                for (int i = 0; i < batchEventList.size(); i++) {
                    BatchEvent batchEvent = batchEventList.get(i);
                    this.batchHandler.handle(batchEvent.getType(), batchEvent.getOffset(), batchEvent.getBatch(), i == batchEventList.size() - 1);
                    batchEvent.clear();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
