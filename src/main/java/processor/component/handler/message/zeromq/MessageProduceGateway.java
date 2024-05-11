package processor.component.handler.message.zeromq;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import lombok.Getter;
import utils.logging.LoggerUtil;

@Getter
public class MessageProduceGateway {
    private final GridgoContext context;

    public MessageProduceGateway(String replyTo) {
        this.context = new DefaultGridgoContextBuilder().setName(replyTo).setExceptionHandler(this::onException).build();
        this.context.openGateway(replyTo).attachConnector("zmq:push:" + replyTo);
        this.onStart();
    }

    private void onStart() {
        this.context.start();
    }

    public void onStop() {
        this.context.stop();
    }

    private void onException(Throwable ex) {
        LoggerUtil.logError("Internal error", ex);
    }
}
