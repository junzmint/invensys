package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import processor.component.disruptor.event.message.MessageEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MessageEventProducerTest {

    private RingBuffer<MessageEvent> ringBuffer;
    private MessageEventProducer producer;

    @BeforeEach
    void setUp() {
        ringBuffer = mock(RingBuffer.class);
        producer = new MessageEventProducer(ringBuffer);
    }

    @Test
    void testOnData() {
        String corrId = "testCorrId";
        String replyTo = "testReplyTo";
        String body = "testBody";

        producer.onData(corrId, replyTo, body);

        ArgumentCaptor<EventTranslatorThreeArg<MessageEvent, String, String, String>> translatorCaptor = ArgumentCaptor.forClass(EventTranslatorThreeArg.class);
        verify(ringBuffer).publishEvent(translatorCaptor.capture(), eq(corrId), eq(replyTo), eq(body));

        MessageEvent event = new MessageEvent();
        long sequence = 0L; // The sequence number, usually provided by the RingBuffer
        translatorCaptor.getValue().translateTo(event, sequence, corrId, replyTo, body);

        assertEquals(corrId, event.getCorrId());
        assertEquals(replyTo, event.getReplyTo());
        assertEquals(body, event.getBody());
    }

    @Test
    void testClose() {
        producer.close();

        // Using reflection to access the private ringBuffer field
        try {
            var field = MessageEventProducer.class.getDeclaredField("ringBuffer");
            field.setAccessible(true);
            assertNull(field.get(producer));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
