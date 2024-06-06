package processor.component.disruptor.event.message;

import lombok.Getter;
import lombok.Setter;
import processor.component.disruptor.event.Clearable;

@Setter
@Getter
public class MessageEvent implements Clearable {
    private String corrId;
    private String replyTo;
    private String body;

    @Override
    public void clear() {
        this.corrId = null;
        this.replyTo = null;
        this.body = null;
    }
}
