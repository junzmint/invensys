package processor;

import io.gridgo.utils.ThreadUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import processor.component.Processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProcessorApplicationTest {
    @Test
    void testMain() {
        // Mock the static method
        try (MockedStatic<ThreadUtils> mockedStaticThreadUtils = Mockito.mockStatic(ThreadUtils.class);
             MockedConstruction<Processor> mockedConstruction = Mockito.mockConstruction(Processor.class,
                     (mock, context) -> {
                         doNothing().when(mock).start();
                         doNothing().when(mock).stop();
                     })) {

            // Invoke the main method
            ProcessorApplication.main(new String[]{});

            // Verify that ThreadUtils.registerShutdownTask was called with the correct argument
            mockedStaticThreadUtils.verify(() -> ThreadUtils.registerShutdownTask(any(Runnable.class)), times(1));

            // Verify that the start method was called on the constructed ApiGateway instance
            Processor constructedProcessor = mockedConstruction.constructed().get(0);
            verify(constructedProcessor).start();
        }
    }
}
