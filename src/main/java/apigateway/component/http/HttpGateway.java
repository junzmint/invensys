package apigateway.component.http;

import apigateway.component.kafkaproducer.KafkaProducer;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;

import java.util.concurrent.atomic.AtomicLong;

public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducer kafkaProducer;
    private final AtomicLong corrId;
    private final String replyTo;

    public HttpGateway(String gatewayName, KafkaProducer kafkaProducer, AtomicLong corrId, String replyTo) {
        super(gatewayName);
        this.kafkaProducer = kafkaProducer;
        this.corrId = corrId;
        this.replyTo = replyTo;
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();
        var deferred = rc.getDeferred();

        // Attach corrId and reply address to header
        Long id = corrId.getAndIncrement();
        message.headers().setAny("corrId", id);
        message.headers().setAny("replyTo", replyTo);
        
        // Generate Kafka key
        String key = "Hello, world!!";
        kafkaProducer.produce(message, deferred, key);
    }
}
