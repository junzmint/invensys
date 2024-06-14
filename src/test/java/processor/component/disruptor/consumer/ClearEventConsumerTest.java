package processor.component.disruptor.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processor.component.disruptor.event.Clearable;

import static org.mockito.Mockito.*;

class ClearEventConsumerTest {

    private ClearEventConsumer<Clearable> clearEventConsumer;
    private Clearable clearableEvent;

    @BeforeEach
    void setUp() {
        clearEventConsumer = new ClearEventConsumer<>();
        clearableEvent = mock(Clearable.class);
    }

    @Test
    void testOnEventWithClearable() throws Exception {
        clearEventConsumer.onEvent(clearableEvent, 0, true);
        verify(clearableEvent, times(1)).clear();
    }

    @Test
    void testOnEventWithNonClearable() throws Exception {
        Object nonClearableEvent = mock(Object.class);
        ClearEventConsumer<Object> consumer = new ClearEventConsumer<>();

        consumer.onEvent(nonClearableEvent, 0, true);

        // Verify that no interactions occurred on the non-clearable event
        verifyNoInteractions(nonClearableEvent);
    }
}
