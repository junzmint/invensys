package processor.component.handler.message;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;
import processor.component.handler.message.zeromq.MessageProduceGateway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {
    // we can have many api gateways, so we need to open a message produce gateway for each of the api gateways
    // this map is message produce gateways by reply address
    private final Map<String, MessageProduceGateway> messageProduceGatewayMap;

    public MessageHandler() {
        this.messageProduceGatewayMap = new ConcurrentHashMap<>();
    }

    public void send(String replyTo, String corrId, String body) {
        Message message = Message.ofAny(BElement.ofAny(body));
        message.headers().setAny("corrId", corrId);

        MessageProduceGateway messageProduceGateway = this.messageProduceGatewayMap.get(replyTo);

        if (messageProduceGateway != null) {
            messageProduceGateway.getContext().findGatewayMandatory(replyTo).send(message);
        } else {
            messageProduceGateway = new MessageProduceGateway(replyTo);
            messageProduceGateway.getContext().findGatewayMandatory(replyTo).send(message);
            this.messageProduceGatewayMap.put(replyTo, messageProduceGateway);
        }
    }

    private void onStart() {
        // nothing to do here
    }

    public void close() {
        for (Map.Entry<String, MessageProduceGateway> entry : this.messageProduceGatewayMap.entrySet()) {
            entry.getValue().onStop();
        }
        this.messageProduceGatewayMap.clear();
    }
}
