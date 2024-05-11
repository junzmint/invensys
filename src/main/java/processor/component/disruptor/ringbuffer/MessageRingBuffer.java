package processor.component.disruptor.ringbuffer;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.MessageEventConsumer;
import processor.component.disruptor.event.message.MessageEvent;
import processor.component.disruptor.event.message.MessageEventFactory;

public class MessageRingBuffer {
    private final MessageEventFactory factory;
    private final int bufferSize;
    private final MessageEventConsumer messageEventConsumer;
    private final ClearEventConsumer<MessageEvent> clearEventConsumer;

    public MessageRingBuffer(MessageEventFactory factory, int bufferSize, MessageEventConsumer messageEventConsumer, ClearEventConsumer<MessageEvent> clearEventConsumer) {
        this.factory = factory;
        this.bufferSize = bufferSize;
        this.messageEventConsumer = messageEventConsumer;
        this.clearEventConsumer = clearEventConsumer;
    }

    public RingBuffer<MessageEvent> getRingBuffer() {
        Disruptor<MessageEvent> disruptor =
                new Disruptor<>(this.factory, this.bufferSize, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(this.messageEventConsumer).then(this.clearEventConsumer);
        disruptor.start();

        return disruptor.getRingBuffer();
    }
}
