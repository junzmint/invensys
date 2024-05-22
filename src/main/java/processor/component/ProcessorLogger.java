package processor.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ProcessorLogger {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorLogger.class);
    private static final Marker CACHE_STAT_MARKER = MarkerFactory.getMarker("CACHE_STAT");

    public static void logCacheStat(String message) {
        logger.info(CACHE_STAT_MARKER, message);
    }
}
