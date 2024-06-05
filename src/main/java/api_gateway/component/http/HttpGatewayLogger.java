package api_gateway.component.http;

import api_gateway.component.ApiGatewayLogger;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class HttpGatewayLogger extends ApiGatewayLogger {
    private static final Logger logger = LoggerFactory.getLogger(HttpGatewayLogger.class);
    private static final Marker HTTP_REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");

    public static void logHttpRequest(Message message, Deferred<Message, Exception> deferred) {
        logger.info(HTTP_REQUEST_MARKER, "POST: {}\nfrom {}", message.body().toJson(), deferred.toString());
    }
}
