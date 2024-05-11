package processor.component;

import utils.logging.LoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProcessorConstants {
    private static final String CONFIG_FILE_PATH = "config/application.properties";
    private static final Properties ProcessorProps;

    static {
        ProcessorProps = new Properties();
        try {
            ProcessorProps.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            LoggerUtil.logError(e.getMessage());
        }
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
}
