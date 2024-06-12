package processor.component.disruptor.producer;

import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import processor.component.disruptor.event.batch.BatchEvent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BatchEventProducerTest {

    private RingBuffer<BatchEvent> ringBuffer;
    private BatchEventProducer producer;

    @BeforeEach
    void setUp() {
        ringBuffer = mock(RingBuffer.class);
        producer = new BatchEventProducer(ringBuffer);
    }

    @Test
    void testOnData() {
        String type = "testType";
        Long offset = 123L;
        Map<String, Long> batch = new HashMap<>();
        batch.put("item1", 100L);
        batch.put("item2", 200L);

        producer.onData(type, offset, batch);

        ArgumentCaptor<EventTranslatorThreeArg<BatchEvent, String, Long, Map<String, Long>>> translatorCaptor = ArgumentCaptor.forClass(EventTranslatorThreeArg.class);
        verify(ringBuffer).publishEvent(translatorCaptor.capture(), eq(type), eq(offset), eq(batch));

        BatchEvent event = new BatchEvent();
        long sequence = 0L; // The sequence number, usually provided by the RingBuffer
        translatorCaptor.getValue().translateTo(event, sequence, type, offset, batch);

        assertEquals(type, event.getType());
        assertEquals(offset, event.getOffset());
        assertEquals(batch, event.getBatch());
    }

    @Test
    void testClose() {
        producer.close();

        // Using reflection to access the private ringBuffer field
        try {
            var field = BatchEventProducer.class.getDeclaredField("ringBuffer");
            field.setAccessible(true);
            assertNull(field.get(producer));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
