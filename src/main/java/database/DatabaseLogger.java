package database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DatabaseLogger {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseLogger.class);
    private static final Marker DATABASE_INFO_MARKER = MarkerFactory.getMarker("DATABASE_INFO");
    private static final Marker DATABASE_ERROR_MARKER = MarkerFactory.getMarker("DATABASE_ERROR");

    public static void logDatabaseInfo(String message, StackTraceElement[] stackTrace) {
        logger.info(DATABASE_INFO_MARKER, "in {}: {}", stackTrace[1], message);
    }

    public static void logDatabaseError(String message, Exception exception) {
        logger.error(DATABASE_ERROR_MARKER, message, exception);
    }
}
