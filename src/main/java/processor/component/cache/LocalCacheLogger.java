package processor.component.cache;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import processor.component.ProcessorLogger;

public class LocalCacheLogger extends ProcessorLogger {
    private static final Marker CACHE_STAT_MARKER = MarkerFactory.getMarker("CACHE_STAT");
    @Setter
    private static Logger logger = LoggerFactory.getLogger(LocalCacheLogger.class);

    public static void logCacheStat(String message) {
        logger.info(CACHE_STAT_MARKER, message);
    }
}
