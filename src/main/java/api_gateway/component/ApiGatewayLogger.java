package api_gateway.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ApiGatewayLogger {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayLogger.class);
    private static final Marker API_GATEWAY_ERROR_MARKER = MarkerFactory.getMarker("API_GATEWAY_ERROR");

    public static void logApiGatewayError(String message, Throwable exception) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, exception);
    }

    public static void logApiGatewayError(String message, Exception exception) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, exception);
    }
}
