package processor.component.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import processor.component.disruptor.event.message.MessageEvent;
import processor.component.handler.message.MessageHandler;

public class MessageEventConsumer implements EventHandler<MessageEvent> {
    private final MessageHandler messageHandler;

    public MessageEventConsumer(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) {
        this.messageHandler.send(event.getReplyTo(), event.getCorrId(), event.getBody());
    }
}
