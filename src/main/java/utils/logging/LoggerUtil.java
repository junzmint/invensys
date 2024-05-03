package utils.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void logInfo(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    public static void logWarn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void logError(String message) {
        logger.error(message);
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logWarn(String message) {
        logger.warn(message);
    }
}