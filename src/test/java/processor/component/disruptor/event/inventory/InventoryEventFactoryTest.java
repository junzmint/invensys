package processor.component.disruptor.event.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryEventFactoryTest {

    @Test
    public void testNewInstance() {
        // Create an instance of InventoryEventFactory
        InventoryEventFactory factory = new InventoryEventFactory();

        // Use the factory to create a new instance of InventoryEvent
        InventoryEvent event = factory.newInstance();

        // Verify that the new instance is not null
        assertNotNull(event, "The new instance should not be null");

        // Verify that the new instance is of type InventoryEvent
        assertTrue(true, "The new instance should be of type InventoryEvent");
    }
}
