package processor.component.disruptor.event.batch;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BatchEventTest {

    @Test
    public void testSetBatch() {
        // Create an instance of BatchEvent
        BatchEvent event = new BatchEvent();

        // Create a batch to set
        Map<String, Long> batch = new HashMap<>();
        batch.put("key1", 1L);
        batch.put("key2", 2L);

        // Set the batch
        event.setBatch(batch);

        // Verify the batch is set correctly
        assertNotNull(event.getBatch(), "The batch should not be null");
        assertEquals(2, event.getBatch().size(), "The batch should contain 2 entries");
        assertEquals(1L, event.getBatch().get("key1"), "The batch should contain the correct value for key1");
        assertEquals(2L, event.getBatch().get("key2"), "The batch should contain the correct value for key2");

        // Verify the batch is a copy, not the original reference
        batch.put("key3", 3L);
        assertNull(event.getBatch().get("key3"), "The batch should not be affected by changes to the original map");
    }

    @Test
    public void testClear() {
        // Create an instance of BatchEvent
        BatchEvent event = new BatchEvent();

        // Set some properties
        event.setType("type1");
        event.setOffset(100L);
        Map<String, Long> batch = new HashMap<>();
        batch.put("key1", 1L);
        event.setBatch(batch);

        // Clear the event
        event.clear();

        // Verify all properties are cleared
        assertNull(event.getType(), "The type should be null");
        assertNull(event.getOffset(), "The offset should be null");
        assertNull(event.getBatch(), "The batch should be null");
    }
}
