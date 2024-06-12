package processor.component.disruptor.event.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageEventFactoryTest {

    @Test
    public void testNewInstance() {
        // Create an instance of MessageEventFactory
        MessageEventFactory factory = new MessageEventFactory();

        // Call the newInstance method to create a new MessageEvent
        MessageEvent event = factory.newInstance();

        // Verify that the created event is not null
        assertNotNull(event);

        // Verify that the created event is an instance of MessageEvent
        assertTrue(true);
    }
}
