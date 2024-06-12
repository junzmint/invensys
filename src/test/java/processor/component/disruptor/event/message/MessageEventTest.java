package processor.component.disruptor.event.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MessageEventTest {

    @Test
    public void testSetAndGetProperties() {
        // Create an instance of MessageEvent
        MessageEvent event = new MessageEvent();

        // Set properties
        event.setCorrId("12345");
        event.setReplyTo("replyQueue");
        event.setBody("This is a test message.");

        // Verify that the properties are set correctly
        assertEquals("12345", event.getCorrId());
        assertEquals("replyQueue", event.getReplyTo());
        assertEquals("This is a test message.", event.getBody());
    }

    @Test
    public void testClear() {
        // Create an instance of MessageEvent
        MessageEvent event = new MessageEvent();

        // Set properties
        event.setCorrId("12345");
        event.setReplyTo("replyQueue");
        event.setBody("This is a test message.");

        // Clear the properties
        event.clear();

        // Verify that the properties are cleared
        assertNull(event.getCorrId());
        assertNull(event.getReplyTo());
        assertNull(event.getBody());
    }
}
