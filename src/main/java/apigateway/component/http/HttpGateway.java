package apigateway.component.http;

import apigateway.component.kafkaproducer.KafkaProducer;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducer kafkaProducer;
    private final AtomicLong corrId;
    private final String replyTo;
    private final Map<String, Deferred<Message, Exception>> deferredMap;

    public HttpGateway(String gatewayName, KafkaProducer kafkaProducer, Map<String, Deferred<Message, Exception>> deferredMap, AtomicLong corrId, String replyTo) {
        super(gatewayName);
        this.kafkaProducer = kafkaProducer;
        this.corrId = corrId;
        this.replyTo = replyTo;
        this.deferredMap = deferredMap;
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();
        var deferred = rc.getDeferred();

        // Attach corrId and reply address to header
        long id = this.corrId.getAndIncrement();
        message.headers().setAny("corrId", Long.toString(id));
        message.headers().setAny("replyTo", this.replyTo);

        this.deferredMap.put(Long.toString(id), deferred);

        // Generate Kafka keys
        String key = "Hello, world!!";
        this.kafkaProducer.produce(message, deferred, key, false);
    }
}
