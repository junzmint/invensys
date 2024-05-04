package apigateway.component.http;

import apigateway.component.kafkaproducer.KafkaProducer;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;

public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducer kafkaProducer;

    public HttpGateway(String gatewayName, KafkaProducer kafkaProducer) {
        super(gatewayName);
        this.kafkaProducer = kafkaProducer;
    }


    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var msg = rc.getMessage();
        var deferred = rc.getDeferred();

        kafkaProducer.send(msg, deferred);
        // deferred.resolve(Message.ofAny(msg.body()));
    }
}
