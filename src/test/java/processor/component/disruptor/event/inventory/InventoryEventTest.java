package processor.component.disruptor.event.inventory;

import io.gridgo.framework.support.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class InventoryEventTest {

    private InventoryEvent inventoryEvent;
    private Message mockMessage;

    @BeforeEach
    public void setUp() {
        inventoryEvent = new InventoryEvent();
        mockMessage = mock(Message.class);
    }

    @Test
    public void testClear() {
        // Set some values
        inventoryEvent.setOffSet(123L);
        inventoryEvent.setMessage(mockMessage);

        // Verify values are set
        assertEquals(123L, inventoryEvent.getOffSet());
        assertEquals(mockMessage, inventoryEvent.getMessage());

        // Clear the values
        inventoryEvent.clear();

        // Verify values are cleared
        assertNull(inventoryEvent.getOffSet());
        assertNull(inventoryEvent.getMessage());
    }

    @Test
    public void testSettersAndGetters() {
        // Set values using setters
        inventoryEvent.setOffSet(456L);
        inventoryEvent.setMessage(mockMessage);

        // Verify values using getters
        assertEquals(456L, inventoryEvent.getOffSet());
        assertEquals(mockMessage, inventoryEvent.getMessage());
    }
}
