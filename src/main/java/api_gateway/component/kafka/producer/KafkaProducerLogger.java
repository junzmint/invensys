package api_gateway.component.kafka.producer;

import api_gateway.component.ApiGatewayLogger;
import io.gridgo.framework.support.Message;
import lombok.Setter;
import org.joo.promise4j.Deferred;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class KafkaProducerLogger extends ApiGatewayLogger {
    private static final Marker KAFKA_PRODUCER_INFO_MARKER = MarkerFactory.getMarker("KAFKA_PRODUCER_INFO");
    @Setter
    private static Logger logger = LoggerFactory.getLogger(KafkaProducerLogger.class);

    public static void logKafkaProducerInfo(String message, Deferred<Message, Exception> deferred) {
        logger.info(KAFKA_PRODUCER_INFO_MARKER, "{} for request from {}", message, deferred.toString());
    }

    public static void logKafkaProducerInfo(String message, String ackMessage, Deferred<Message, Exception> deferred) {
        logger.info(KAFKA_PRODUCER_INFO_MARKER, "{} for request from {} with ack message {}", message, deferred.toString(), ackMessage);
    }
}
