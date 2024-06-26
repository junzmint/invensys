package processor.component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProcessorConstants {
    private static final Properties ProcessorProps;
    private static String configFilePath = "config/processor/processor.properties";

    static {
        ProcessorProps = new Properties();
        loadProperties();
    }

    private static void loadProperties() {
        try {
            ProcessorProps.load(new FileInputStream(configFilePath));
        } catch (IOException exception) {
            ProcessorLogger.logProcessorError("CONFIG_LOADER_ERROR", exception);
        }
    }

    public static void setConfigFilePath(String path) {
        configFilePath = path;
        loadProperties();
    }

    public static String getKafkaBroker() {
        return ProcessorProps.getProperty("KAFKA_BROKER");
    }

    public static String getKafkaTopic() {
        return ProcessorProps.getProperty("KAFKA_TOPIC");
    }

    public static String getDeserializerClassConfig() {
        return ProcessorProps.getProperty("DESERIALIZER_CLASS_CONFIG");
    }

    public static String getGroupId() {
        return ProcessorProps.getProperty("GROUP_ID");
    }

    public static String getAutoOffsetReset() {
        return ProcessorProps.getProperty("AUTO_OFFSET_RESET");
    }

    public static Long getCacheSize() {
        return Long.parseLong(ProcessorProps.getProperty("CACHE_SIZE"));
    }

    public static Long getCacheInitRecords() {
        return Long.parseLong(ProcessorProps.getProperty("CACHE_INIT_RECORDS"));
    }

    // after CACHE_LOG_AFTER secs, logs cache statistics
    public static Long getCacheStatLogAfter() {
        return Long.parseLong(ProcessorProps.getProperty("CACHE_STAT_LOG_AFTER"));
    }
}
