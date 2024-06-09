package api_gateway;

import api_gateway.component.ApiGateway;
import io.gridgo.utils.ThreadUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ApiGatewayApplicationTest {

    @Test
    void testMain() {
        // Mock the static method
        try (MockedStatic<ThreadUtils> mockedStaticThreadUtils = Mockito.mockStatic(ThreadUtils.class);
             MockedConstruction<ApiGateway> mockedConstruction = Mockito.mockConstruction(ApiGateway.class,
                     (mock, context) -> {
                         doNothing().when(mock).start();
                         doNothing().when(mock).stop();
                     })) {

            // Invoke the main method
            ApiGatewayApplication.main(new String[]{});

            // Verify that ThreadUtils.registerShutdownTask was called with the correct argument
            mockedStaticThreadUtils.verify(() -> ThreadUtils.registerShutdownTask(any(Runnable.class)), times(1));

            // Verify that the start method was called on the constructed ApiGateway instance
            ApiGateway constructedApiGateway = mockedConstruction.constructed().get(0);
            verify(constructedApiGateway).start();
        }
    }
}
