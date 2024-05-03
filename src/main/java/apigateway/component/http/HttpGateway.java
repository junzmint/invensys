package apigateway.component.http;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;

public class HttpGateway extends HttpGatewayBaseComponent {
    public HttpGateway(String gatewayName) {
        super(gatewayName);
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var msg = rc.getMessage();
        var deferred = rc.getDeferred();

        deferred.resolve(Message.ofAny(msg.body()));
    }
}
