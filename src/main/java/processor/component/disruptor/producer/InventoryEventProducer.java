package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;
import io.gridgo.framework.support.Message;
import processor.component.disruptor.event.inventory.InventoryEvent;

public class InventoryEventProducer {
    private static final EventTranslatorTwoArg<InventoryEvent, Long, Message> TRANSLATOR =
            new EventTranslatorTwoArg<InventoryEvent, Long, Message>() {
                @Override
                public void translateTo(InventoryEvent event, long sequence, Long offSet, Message message) {
                    event.setOffSet(offSet);
                    event.setMessage(message);
                }
            };

    private final RingBuffer<InventoryEvent> ringBuffer;

    public InventoryEventProducer(RingBuffer<InventoryEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(Long offSet, Message message) {
        this.ringBuffer.publishEvent(TRANSLATOR, offSet, message);
    }

    public void close() {
        // nothing to do here
    }
}
