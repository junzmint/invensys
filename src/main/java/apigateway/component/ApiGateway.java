package apigateway.component;

import apigateway.component.http.HttpGateway;
import apigateway.component.kafkaproducer.KafkaProducer;
import apigateway.component.kafkaproducer.KafkaProducerConfig;
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

        KafkaProducerConfig kafkaProducerConfig = new KafkaProducerConfig(
                ApiGatewayConstants.getKafkaBroker(),
                ApiGatewayConstants.getSerializerClassConfig(),
                ApiGatewayConstants.getSerializerClassConfig(),
                "all",
                ApiGatewayConstants.getKafkaTopic(),
                0);

        KafkaProducer kafkaProducer = new KafkaProducer(kafkaProducerConfig);

        HttpGateway httpGateway = new HttpGateway(HTTP_GATEWAY, kafkaProducer);

        this.appContext.openGateway(HTTP_GATEWAY).attachConnector(VERTX_URL);
        // handle http request
        this.appContext.attachComponent(httpGateway);
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
