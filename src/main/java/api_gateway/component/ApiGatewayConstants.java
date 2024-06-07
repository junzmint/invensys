package api_gateway.component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiGatewayConstants {
    private static final Properties ApiGatewayProps;
    private static String configFilePath = "config/api_gateway/api_gateway.properties";

    static {
        ApiGatewayProps = new Properties();
        loadProperties();
    }

    private static void loadProperties() {
        try {
            ApiGatewayProps.load(new FileInputStream(configFilePath));
        } catch (IOException exception) {
            ApiGatewayLogger.logApiGatewayError("CONFIG_LOADER_ERROR", exception);
        }
    }

    public static void setConfigFilePath(String path) {
        configFilePath = path;
        loadProperties();
    }

    public static String getVertxUrl() {
        return ApiGatewayProps.getProperty("VERTX_URL");
    }

    public static String getApiGateway() {
        return ApiGatewayProps.getProperty("API_GATEWAY");
    }

    public static String getHttpGateway() {
        return ApiGatewayProps.getProperty("HTTP_GATEWAY");
    }

    public static String getZMQPullGateway() {
        return ApiGatewayProps.getProperty("ZMQ_PULL_GATEWAY");
    }

    public static String getZMQReplyAddress() {
        return ApiGatewayProps.getProperty("REPLY_TO");
    }

    public static String getKafkaBroker() {
        return ApiGatewayProps.getProperty("KAFKA_BROKER");
    }

    public static String getKafkaTopic() {
        return ApiGatewayProps.getProperty("KAFKA_TOPIC");
    }

    public static String getSerializerClassConfig() {
        return ApiGatewayProps.getProperty("SERIALIZER_CLASS_CONFIG");
    }
}

