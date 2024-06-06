package processor;

import io.gridgo.utils.ThreadUtils;
import processor.component.Processor;

public class ProcessorApplication {
    public static void main(String[] args) {
        final var app = new Processor();
        ThreadUtils.registerShutdownTask(app::stop);
        app.start();
    }
}
