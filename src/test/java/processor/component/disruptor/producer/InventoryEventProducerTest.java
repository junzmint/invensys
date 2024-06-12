package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;
import io.gridgo.framework.support.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import processor.component.disruptor.event.inventory.InventoryEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InventoryEventProducerTest {

    private RingBuffer<InventoryEvent> ringBuffer;
    private InventoryEventProducer producer;

    @BeforeEach
    void setUp() {
        ringBuffer = mock(RingBuffer.class);
        producer = new InventoryEventProducer(ringBuffer);
    }

    @Test
    void testOnData() {
        Long offset = 123L;
        Message message = mock(Message.class);

        producer.onData(offset, message);

        ArgumentCaptor<EventTranslatorTwoArg<InventoryEvent, Long, Message>> translatorCaptor = ArgumentCaptor.forClass(EventTranslatorTwoArg.class);
        verify(ringBuffer).publishEvent(translatorCaptor.capture(), eq(offset), eq(message));

        InventoryEvent event = new InventoryEvent();
        long sequence = 0L; // The sequence number, usually provided by the RingBuffer
        translatorCaptor.getValue().translateTo(event, sequence, offset, message);

        assertEquals(offset, event.getOffSet());
        assertEquals(message, event.getMessage());
    }

    @Test
    void testClose() {
        producer.close();

        // Using reflection to access the private ringBuffer field
        try {
            var field = InventoryEventProducer.class.getDeclaredField("ringBuffer");
            field.setAccessible(true);
            assertNull(field.get(producer));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
