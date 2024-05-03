package apigateway;

import apigateway.component.ApiGateway;
import io.gridgo.utils.ThreadUtils;

public class ApiGatewayApplication {

    public static void main(String[] args) {
        final var app = new ApiGateway();
        ThreadUtils.registerShutdownTask(app::stop);
        app.start();
    }
}
