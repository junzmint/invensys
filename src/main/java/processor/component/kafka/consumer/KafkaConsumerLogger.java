package processor.component.kafka.consumer;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import processor.component.ProcessorLogger;

public class KafkaConsumerLogger extends ProcessorLogger {
    private static final Marker KAFKA_CONSUMER_INFO_MARKER = MarkerFactory.getMarker("KAFKA_CONSUMER_INFO");
    @Setter
    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerLogger.class);

    public static void logKafkaConsumerInfo(String message) {
        logger.info(KAFKA_CONSUMER_INFO_MARKER, message);
    }
}
