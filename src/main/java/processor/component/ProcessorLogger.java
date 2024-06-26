package processor.component;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ProcessorLogger {
    private static final Marker PROCESSOR_ERROR_MARKER = MarkerFactory.getMarker("PROCESSOR_ERROR");
    @Setter
    private static Logger logger = LoggerFactory.getLogger(ProcessorLogger.class);

    public static void logProcessorError(String message, Exception exception) {
        logger.error(PROCESSOR_ERROR_MARKER, message, exception);
    }

    public static void logProcessorError(String message, Throwable exception) {
        logger.error(PROCESSOR_ERROR_MARKER, message, exception);
    }
}
