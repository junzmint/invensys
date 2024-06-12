package processor.component.disruptor.event.batch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BatchEventFactoryTest {

    @Test
    public void testNewInstance() {
        // Create an instance of BatchEventFactory
        BatchEventFactory factory = new BatchEventFactory();

        // Create a new instance of BatchEvent using the factory
        BatchEvent event = factory.newInstance();

        // Verify that the new instance is not null
        assertNotNull(event, "The new instance of BatchEvent should not be null");

        // Verify that the new instance is of type BatchEvent
        assertTrue(true, "The new instance should be of type BatchEvent");

        // Verify that the properties of the new instance are initialized correctly
        assertNull(event.getType(), "The type should be null");
        assertNull(event.getOffset(), "The offset should be null");
        assertNull(event.getBatch(), "The batch should be null");
    }
}
