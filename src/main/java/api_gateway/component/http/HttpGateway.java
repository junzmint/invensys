package api_gateway.component.http;

import api_gateway.component.kafka.producer.KafkaProducer;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import org.joo.promise4j.Deferred;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducer kafkaProducer;
    private final String replyTo;
    private final Map<String, Deferred<Message, Exception>> deferredMap;
    @Getter
    private final AtomicLong corrId;

    public HttpGateway(String gatewayName, KafkaProducer kafkaProducer, Map<String, Deferred<Message, Exception>> deferredMap, String replyTo) {
        super(gatewayName);
        this.kafkaProducer = kafkaProducer;
        this.corrId = new AtomicLong(0);
        this.replyTo = replyTo;
        this.deferredMap = deferredMap;
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();
        var deferred = rc.getDeferred();
        // generate corrId and reply address
        long id = this.corrId.getAndIncrement();
        // create kafka message key
        String key = Long.toString(id);
        // attach corrId and reply address to header
        message.headers().setAny("corrId", key);
        message.headers().setAny("replyTo", this.replyTo);
        // store corrId
        this.deferredMap.put(key, deferred);
        // kafka produce
        this.kafkaProducer.produce(message, deferred, key, false);
        // log client request
        HttpGatewayLogger.logHttpRequest(message, deferred);
    }
}
