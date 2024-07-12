package processor.component.queue.producer;

import processor.component.queue.event.message.MessageEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class MessageEventProducer {
    private final ArrayBlockingQueue<MessageEvent> messageEventArrayBlockingQueue;

    public MessageEventProducer(ArrayBlockingQueue<MessageEvent> queue) {
        this.messageEventArrayBlockingQueue = queue;
    }

    public void onData(String corrId, String replyTo, String body) {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCorrId(corrId);
        messageEvent.setReplyTo(replyTo);
        messageEvent.setBody(body);

        try {
            this.messageEventArrayBlockingQueue.put(messageEvent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
