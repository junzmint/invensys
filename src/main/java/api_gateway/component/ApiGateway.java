package api_gateway.component;

import api_gateway.component.http.HttpGateway;
import api_gateway.component.kafka.producer.KafkaProducer;
import api_gateway.component.kafka.producer.KafkaProducerConfig;
import api_gateway.component.message.MessageReceiveGateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiGateway extends NonameComponentLifecycle {
    // constants
    private static final String HTTP_GATEWAY = ApiGatewayConstants.getHttpGateway();
    private static final String ZMQ_PULL_GATEWAY = ApiGatewayConstants.getZMQPullGateway();
    private static final String VERTX_URL = ApiGatewayConstants.getVertxUrl();
    private static final String ZMQ_REPLY_ADR = ApiGatewayConstants.getZMQReplyAddress();
    private static final String API_GATEWAY = ApiGatewayConstants.getApiGateway();

    // components
    private final GridgoContext appContext;
    private final KafkaProducer kafkaProducer;
    private final HttpGateway httpGateway;
    private final Map<String, Deferred<Message, Exception>> deferredMap;
    private final MessageReceiveGateway messageReceiveGateway;

    // init components and inject components
    public ApiGateway() {
        this.deferredMap = new ConcurrentHashMap<>();
        this.appContext = new DefaultGridgoContextBuilder().setName(API_GATEWAY).setExceptionHandler(this::onException).build();

        KafkaProducerConfig kafkaProducerConfig = new KafkaProducerConfig(
                ApiGatewayConstants.getKafkaBroker(),
                ApiGatewayConstants.getSerializerClassConfig(),
                ApiGatewayConstants.getSerializerClassConfig(),
                "all",
                ApiGatewayConstants.getKafkaTopic(),
                0);

        this.kafkaProducer = new KafkaProducer(kafkaProducerConfig);

        this.httpGateway = new HttpGateway(
                HTTP_GATEWAY,
                this.kafkaProducer,
                this.deferredMap,
                ZMQ_REPLY_ADR);

        this.messageReceiveGateway = new MessageReceiveGateway(ZMQ_PULL_GATEWAY, this.deferredMap);

        // open gateway
        this.appContext.openGateway(HTTP_GATEWAY).attachConnector(VERTX_URL);
        this.appContext.openGateway(ZMQ_PULL_GATEWAY).attachConnector("zmq:pull:" + ZMQ_REPLY_ADR);
        // handle http request
        this.appContext.attachComponent(this.httpGateway);
        // handle zero mq message
        this.appContext.attachComponent(this.messageReceiveGateway);
    }

    private void onException(Throwable exception) {
        ApiGatewayLogger.logApiGatewayError("INTERNAL_ERROR", exception);
    }

    @Override
    protected void onStart() {
        this.appContext.start();
    }

    @Override
    protected void onStop() {
        this.kafkaProducer.onClose();
        this.httpGateway.stop();
        this.messageReceiveGateway.stop();
        this.deferredMap.clear();
        this.appContext.stop();
    }
}
