package processor.component.queue.consumer;

import processor.component.handler.message.MessageHandler;
import processor.component.queue.event.message.MessageEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class MessageEventConsumer implements Runnable {
    private final ArrayBlockingQueue<MessageEvent> messageEventArrayBlockingQueue;
    private final MessageHandler messageHandler;

    public MessageEventConsumer(ArrayBlockingQueue<MessageEvent> queue, MessageHandler messageHandler) {
        this.messageEventArrayBlockingQueue = queue;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                MessageEvent messageEvent = messageEventArrayBlockingQueue.take();
                this.messageHandler.send(messageEvent.getReplyTo(), messageEvent.getCorrId(), messageEvent.getBody());
                messageEvent.clear();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
