package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import processor.component.disruptor.event.message.MessageEvent;

public class MessageEventProducer {
    private static final EventTranslatorThreeArg<MessageEvent, String, String, String> TRANSLATOR =
            new EventTranslatorThreeArg<MessageEvent, String, String, String>() {
                @Override
                public void translateTo(MessageEvent messageEvent, long sequence, String corrId, String replyTo, String body) {
                    messageEvent.setCorrId(corrId);
                    messageEvent.setReplyTo(replyTo);
                    messageEvent.setBody(body);
                }
            };

    private RingBuffer<MessageEvent> ringBuffer;

    public MessageEventProducer(RingBuffer<MessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(String corrId, String replyTo, String body) {
        this.ringBuffer.publishEvent(TRANSLATOR, corrId, replyTo, body);
    }

    public void close() {
        this.ringBuffer = null;
    }
}
