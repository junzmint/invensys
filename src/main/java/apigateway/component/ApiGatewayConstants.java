package apigateway.component;

import logging.LoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiGatewayConstants {
    private static final String CONFIG_FILE_PATH = "config/application.properties";
    private static final Properties ApiGatewayProps;

    static {
        ApiGatewayProps = new Properties();
        try {
            ApiGatewayProps.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            LoggerUtil.logError(e.getMessage());
        }
    }

    public static String getVertxUrl() {
        return ApiGatewayProps.getProperty("VERTX_URL");
    }

    public static String getHttpGateway() {
        return ApiGatewayProps.getProperty("HTTP_GATEWAY");
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

    public static String getZMQReplyAddress() {
        return ApiGatewayProps.getProperty("REPLY_TO");
    }
}

