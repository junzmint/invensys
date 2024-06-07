package api_gateway.component;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ApiGatewayLogger {
    private static final Marker API_GATEWAY_ERROR_MARKER = MarkerFactory.getMarker("API_GATEWAY_ERROR");
    // Setter for testing purposes
    @Setter
    private static Logger logger = LoggerFactory.getLogger(ApiGatewayLogger.class);

    public static void logApiGatewayError(String message, Throwable exception) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, exception);
    }

    public static void logApiGatewayError(String message, Exception exception) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, exception);
    }
}
