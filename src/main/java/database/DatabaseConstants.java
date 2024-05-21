package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConstants {
    private static final String CONFIG_FILE_PATH = "config/database.properties";
    private static final Properties DatabaseProps;

    static {
        DatabaseProps = new Properties();
        try {
            DatabaseProps.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            DatabaseLogger.logDatabaseError("IO_ERROR", e);
        }
    }

    public static String getDatabaseConnection() {
        return DatabaseProps.getProperty("DB_CONNECTION");
    }

    public static String getDatabaseHost() {
        return DatabaseProps.getProperty("DB_HOST");
    }

    public static String getDatabasePort() {
        return DatabaseProps.getProperty("DB_PORT");
    }

    public static String getDatabaseName() {
        return DatabaseProps.getProperty("DB_DATABASE");
    }

    public static String getDatabaseUser() {
        return DatabaseProps.getProperty("DB_USERNAME");
    }

    public static String getDatabasePassword() {
        return DatabaseProps.getProperty("DB_PASSWORD");
    }
}

