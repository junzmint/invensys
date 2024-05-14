package api_gateway.component;

import api_gateway.component.http.HttpGateway;
import api_gateway.component.kafka.producer.KafkaProducer;
import api_gateway.component.kafka.producer.KafkaProducerConfig;
import api_gateway.component.message.MessageReceiveGateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.framework.support.Message;
import logging.LoggerUtil;
import org.joo.promise4j.Deferred;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ApiGateway extends NonameComponentLifecycle {
    private static final String HTTP_GATEWAY = ApiGatewayConstants.getHttpGateway();

    private static final String ZMQ_PULL_GATEWAY = "zero.mq.pull";

    private static final String VERTX_URL = ApiGatewayConstants.getVertxUrl();

    private static final String ZMQ_REPLY_ADR = ApiGatewayConstants.getZMQReplyAddress();

    private static final String API_GATEWAY_NAME = "InvensysApiGateway";
    private final GridgoContext appContext;
    private final KafkaProducer kafkaProducer;
    private final HttpGateway httpGateway;
    private final Map<String, Deferred<Message, Exception>> deferredMap;
    private final MessageReceiveGateway messageReceiveGateway;

    public ApiGateway() {
        this.deferredMap = new ConcurrentHashMap<>();
        this.appContext = new DefaultGridgoContextBuilder().setName(API_GATEWAY_NAME).setExceptionHandler(this::onException).build();

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
                new AtomicLong(0),
                ZMQ_REPLY_ADR);

        this.messageReceiveGateway = new MessageReceiveGateway(ZMQ_PULL_GATEWAY, this.deferredMap);

        this.appContext.openGateway(HTTP_GATEWAY).attachConnector(VERTX_URL);
        this.appContext.openGateway(ZMQ_PULL_GATEWAY).attachConnector("zmq:pull:" + ZMQ_REPLY_ADR);
        // handle http request
        this.appContext.attachComponent(this.httpGateway);
        this.appContext.attachComponent(this.messageReceiveGateway);
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
        this.kafkaProducer.onClose();
        this.httpGateway.stop();
        this.appContext.stop();
    }
}
