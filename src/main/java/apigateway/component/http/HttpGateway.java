package apigateway.component.http;

import apigateway.component.kafkaproducer.KafkaProducer;
import io.gridgo.bean.BObject;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import utils.logging.LoggerUtil;

public class HttpGateway extends HttpGatewayBaseComponent {
    private final KafkaProducer kafkaProducer;

    public HttpGateway(String gatewayName, KafkaProducer kafkaProducer) {
        super(gatewayName);
        this.kafkaProducer = kafkaProducer;
    }

    public boolean isValidRequest(Message message) {
        try {
            BObject body = message.body().asObject();
            if (body == null) {
                return false;
            }

            // Check type
            String type = body.getString("type", null);
            if (!"order".equals(type)) {
                return false;
            }

            // Check orderId
            String orderId = body.getString("orderId", null);
            if (orderId == null || orderId.isEmpty()) {
                return false;
            }

            // Check skuList
            BObject skuList = body.getObject("skuList", null);
            if (skuList == null || skuList.isEmpty()) {
                return false;
            }

            // Check sku
            for (String key : skuList.keySet()) {
                Long quantity = skuList.getLong(key, Long.MAX_VALUE);
                if (quantity == Long.MAX_VALUE) {
                    return false;
                }
                if (quantity <= 0) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            LoggerUtil.logError(e.getMessage());
            return false;
        }
    }

    protected void processRequest(RoutingContext rc, GridgoContext gc) {
        var message = rc.getMessage();
        var deferred = rc.getDeferred();

        if (isValidRequest(message)) {
            // Generate Kafka key
            String key = "Hello, world!!";
            kafkaProducer.produce(message, deferred, key);
        } else {
            deferred.resolve(Message.ofAny("Wrong Json format"));
        }
    }
}
