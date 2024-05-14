package api_gateway.component.message;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;

import java.util.Map;

public class MessageReceiveGateway extends MessageReceiveGatewayBaseComponent {
    private final Map<String, Deferred<Message, Exception>> deferredMap;

    public MessageReceiveGateway(String gatewayName, Map<String, Deferred<Message, Exception>> deferredMap) {
        super(gatewayName);
        this.deferredMap = deferredMap;
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();

        var deferred = this.deferredMap.remove(message.headers().get("corrId"));
        deferred.resolve(Message.ofAny(message.body()));
    }
}
