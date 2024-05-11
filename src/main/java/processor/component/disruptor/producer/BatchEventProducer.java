package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import processor.component.disruptor.event.batch.BatchEvent;

import java.util.Map;

public class BatchEventProducer {
    private static final EventTranslatorThreeArg<BatchEvent, String, Long, Map<String, Long>> TRANSLATOR =
            new EventTranslatorThreeArg<BatchEvent, String, Long, Map<String, Long>>() {
                @Override
                public void translateTo(BatchEvent event, long sequence, String type, Long offset, Map<String, Long> batch) {
                    event.setType(type);
                    event.setOffset(offset);
                    event.setBatch(batch);
                }
            };

    private final RingBuffer<BatchEvent> ringBuffer;

    public BatchEventProducer(RingBuffer<BatchEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(String type, Long offset, Map<String, Long> batch) {
        this.ringBuffer.publishEvent(TRANSLATOR, type, offset, batch);
    }

    public void close() {
        // nothing to here
    }
}
