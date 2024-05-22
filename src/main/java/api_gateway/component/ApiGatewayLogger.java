package api_gateway.component;

import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ApiGatewayLogger {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayLogger.class);
    private static final Marker API_GATEWAY_ERROR_MARKER = MarkerFactory.getMarker("API_GATEWAY_ERROR");
    private static final Marker HTTP_REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
    private static final Marker KAFKA_PRODUCER_INFO_MARKER = MarkerFactory.getMarker("KAFKA_PRODUCER_INFO");
    private static final Marker HTTP_RESPOND_MARKER = MarkerFactory.getMarker("HTTP_RESPOND");

    public static void logApiGatewayError(String message, Throwable ex) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, ex);
    }

    public static void logApiGatewayError(String message, Exception exception) {
        logger.error(API_GATEWAY_ERROR_MARKER, message, exception);
    }

    public static void logHttpRequest(Message message, Deferred<Message, Exception> deferred) {
        logger.info(HTTP_REQUEST_MARKER, "POST: {}\nfrom {}", message.body().toJson(), deferred.toString());
    }

    public static void logKafkaProducerInfo(String message, Deferred<Message, Exception> deferred) {
        logger.info(KAFKA_PRODUCER_INFO_MARKER, "{} for request from {}", message, deferred.toString());
    }

    public static void logKafkaProducerInfo(String message, String ackMessage, Deferred<Message, Exception> deferred) {
        logger.info(KAFKA_PRODUCER_INFO_MARKER, "{} for request from {} with ack message {}", message, deferred.toString(), ackMessage);
    }

    public static void logHttpRespond(String message, Deferred<Message, Exception> deferred) {
        logger.info(HTTP_RESPOND_MARKER, "Resolved: {} to {}", message, deferred.toString());
    }
}
