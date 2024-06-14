package api_gateway.component.message;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import org.joo.promise4j.Deferred;

import java.util.Map;

public class MessageConsumeGateway extends MessageConsumeGatewayBaseComponent {
    private final Map<String, Deferred<Message, Exception>> deferredMap;

    public MessageConsumeGateway(String gatewayName, Map<String, Deferred<Message, Exception>> deferredMap) {
        super(gatewayName);
        this.deferredMap = deferredMap;
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();
        // get connect using message corrId
        var deferred = this.deferredMap.remove(message.headers().get("corrId"));

        if (deferred == null) {
            MessageConsumeGatewayLogger.logApiGatewayError("NULL_DEFERRED", new NullPointerException("deferred"));
        } else {
            // http respond
            try {
                deferred.resolve(Message.ofAny(message.body()));
                MessageConsumeGatewayLogger.logHttpRespond(message.body().toString(), deferred);
            } catch (Exception exception) {
                deferred.reject(exception);
                MessageConsumeGatewayLogger.logApiGatewayError("INTERNAL_ERROR", exception);
            }
        }
    }
}
