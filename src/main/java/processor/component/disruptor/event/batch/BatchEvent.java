package processor.component.disruptor.event.batch;

import lombok.Getter;
import lombok.Setter;
import processor.component.disruptor.event.Clearable;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BatchEvent implements Clearable {
    @Setter
    private String type;
    @Setter
    private Long offset;
    private Map<String, Long> batch;

    @Override
    public void clear() {
        this.type = null;
        this.offset = null;
        this.batch = null;
    }

    public void setBatch(Map<String, Long> batch) {
        this.batch = new HashMap<>(batch);
    }
}
