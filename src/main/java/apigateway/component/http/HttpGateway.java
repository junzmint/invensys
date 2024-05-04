package apigateway.component.http;

import apigateway.component.ApiGatewayConstants;
import apigateway.component.kafkaproducer.KafkaProducer;
import apigateway.component.kafkaproducer.KafkaProducerConfig;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import org.apache.kafka.common.serialization.StringSerializer;

public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducerConfig config = new KafkaProducerConfig(
            ApiGatewayConstants.getKafkaBroker(),
            StringSerializer.class.getName(),
            StringSerializer.class.getName(),
            "1",
            ApiGatewayConstants.getKafkaTopic(),
            0);

    private final KafkaProducer producer = new KafkaProducer(config);

    public HttpGateway(String gatewayName) {
        super(gatewayName);
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var msg = rc.getMessage();
        var deferred = rc.getDeferred();

        producer.send(msg, null);
        // deferred.resolve(Message.ofAny(msg.body()));
    }
}
