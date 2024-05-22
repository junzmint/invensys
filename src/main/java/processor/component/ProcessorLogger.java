package processor.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ProcessorLogger {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorLogger.class);
    private static final Marker CACHE_STAT_MARKER = MarkerFactory.getMarker("CACHE_STAT");
    private static final Marker PROCESSOR_ERROR_MARKER = MarkerFactory.getMarker("PROCESSOR_ERROR");
    private static final Marker KAFKA_CONSUMER_INFO_MARKER = MarkerFactory.getMarker("KAFKA_CONSUMER_INFO");

    public static void logProcessorError(String message, Exception exception) {
        logger.error(PROCESSOR_ERROR_MARKER, message, exception);
    }

    public static void logProcessorError(String message, Throwable exception) {
        logger.error(PROCESSOR_ERROR_MARKER, message, exception);
    }

    public static void logCacheStat(String message) {
        logger.info(CACHE_STAT_MARKER, message);
    }

    public static void logKafkaConsumerInfo(String message) {
        logger.info(KAFKA_CONSUMER_INFO_MARKER, message);
    }
}
