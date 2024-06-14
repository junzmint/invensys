package api_gateway.component.message;

import api_gateway.component.ApiGatewayLogger;
import io.gridgo.framework.support.Message;
import lombok.Setter;
import org.joo.promise4j.Deferred;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class MessageConsumeGatewayLogger extends ApiGatewayLogger {
    private static final Marker HTTP_RESPOND_MARKER = MarkerFactory.getMarker("HTTP_RESPOND");
    @Setter
    private static Logger logger = LoggerFactory.getLogger(MessageConsumeGatewayLogger.class);

    public static void logHttpRespond(String message, Deferred<Message, Exception> deferred) {
        logger.info(HTTP_RESPOND_MARKER, "Resolved: {} to {}", message, deferred.toString());
    }
}
