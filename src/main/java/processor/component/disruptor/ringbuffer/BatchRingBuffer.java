package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import processor.component.disruptor.consumer.BatchEventConsumer;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.event.batch.BatchEvent;
import processor.component.disruptor.event.batch.BatchEventFactory;

public class BatchRingBuffer {
    private final BatchEventFactory factory;
    private final int bufferSize;
    private final BatchEventConsumer batchEventConsumer;
    private final ClearEventConsumer<BatchEvent> clearEventConsumer;

    public BatchRingBuffer(BatchEventFactory factory, int bufferSize, BatchEventConsumer batchEventConsumer, ClearEventConsumer<BatchEvent> clearEventConsumer) {
        this.factory = factory;
        this.bufferSize = bufferSize;
        this.batchEventConsumer = batchEventConsumer;
        this.clearEventConsumer = clearEventConsumer;
    }

    public RingBuffer<BatchEvent> getRingBuffer() {
        Disruptor<BatchEvent> disruptor = new Disruptor<>(
                this.factory,
                this.bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());

        disruptor.handleEventsWith(this.batchEventConsumer).then(this.clearEventConsumer);
        disruptor.start();

        return disruptor.getRingBuffer();
    }
}
