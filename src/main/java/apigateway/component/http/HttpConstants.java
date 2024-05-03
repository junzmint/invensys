package apigateway.component.http;

import utils.logging.LoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HttpConstants {
    private static final String CONFIG_FILE_PATH = "config/application.properties";
    private static final Properties httpGatewayProps;

    static {
        httpGatewayProps = new Properties();
        try {
            httpGatewayProps.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            LoggerUtil.logError(e.getMessage());
        }
    }

    public static String getVertxUrl() {
        return httpGatewayProps.getProperty("VERTX_URL");
    }

    public static String getHttpGateway() {
        return httpGatewayProps.getProperty("HTTP_GATEWAY");
    }
}
