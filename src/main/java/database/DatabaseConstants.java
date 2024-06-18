package database;

import processor.component.ProcessorLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConstants {
    private static final Properties DatabaseProps;
    private static String configFilePath = "config/processor/database.properties";

    static {
        DatabaseProps = new Properties();
        loadProperties();
    }

    private static void loadProperties() {
        try {
            DatabaseProps.load(new FileInputStream(configFilePath));
        } catch (IOException exception) {
            ProcessorLogger.logProcessorError("CONFIG_LOADER_ERROR", exception);
        }
    }

    public static void setConfigFilePath(String path) {
        configFilePath = path;
        loadProperties();
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
