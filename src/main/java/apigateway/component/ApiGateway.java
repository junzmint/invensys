package apigateway.component;

import apigateway.component.http.HttpGateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import utils.logging.LoggerUtil;

public class ApiGateway extends NonameComponentLifecycle {
    private static final String HTTP_GATEWAY = ApiGatewayConstants.getHttpGateway();

    private static final String VERTX_URL = ApiGatewayConstants.getVertxUrl();

    private static final String API_GATEWAY_NAME = "InvensysApiGateway";

    private final GridgoContext appContext;

    public ApiGateway() {
        this.appContext = new DefaultGridgoContextBuilder().setName(API_GATEWAY_NAME).setExceptionHandler(this::onException).build();

        appContext.openGateway(HTTP_GATEWAY).attachConnector(VERTX_URL);

        this.appContext.attachComponent(new HttpGateway(HTTP_GATEWAY));
        // handle http request
    }

    private void onException(Throwable ex) {
        LoggerUtil.logError("Internal error", ex);
    }

    @Override
    protected void onStart() {
        this.appContext.start();
    }

    @Override
    protected void onStop() {
        this.appContext.stop();
    }
}
