package apigateway.component.message;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.ContextAwareComponent;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.impl.NonameComponentLifecycle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

abstract class MessageReceiveGatewayBaseComponent extends NonameComponentLifecycle implements ContextAwareComponent {
    private final String gatewayName;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    private GridgoContext context;

    protected MessageReceiveGatewayBaseComponent(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Override
    protected void onStart() {
        this.getGateway().ifPresent(g -> g.subscribe(this::processRequest));
    }

    @Override
    protected void onStop() {
        this.context.stop();
    }

    protected Optional<GatewaySubscription> getGateway() {
        return this.getContext().getGatewaySubscription(this.gatewayName);
    }

    protected abstract void processRequest(RoutingContext rc, GridgoContext gc);
}
