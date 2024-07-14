package processor.component;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import processor.component.handler.batch.BatchHandler;
import processor.component.handler.inventory.InventoryHandler;
import processor.component.handler.message.MessageHandler;
import processor.component.kafka.consumer.KafkaConsumer;
import processor.component.kafka.consumer.KafkaConsumerConfig;
import processor.component.queue.consumer.BatchEventConsumer;
import processor.component.queue.consumer.InventoryEventConsumer;
import processor.component.queue.consumer.MessageEventConsumer;
import processor.component.queue.event.batch.BatchEvent;
import processor.component.queue.event.inventory.InventoryEvent;
import processor.component.queue.event.message.MessageEvent;
import processor.component.queue.producer.BatchEventProducer;
import processor.component.queue.producer.InventoryEventProducer;
import processor.component.queue.producer.MessageEventProducer;

import java.sql.Connection;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;

public class Processor {
    // constants
    private final static String KAFKA_BROKER = ProcessorConstants.getKafkaBroker();
    private final static String KAFKA_TOPIC = ProcessorConstants.getKafkaTopic();
    private final static String DESERIALIZER_CLASS_CONFIG = ProcessorConstants.getDeserializerClassConfig();
    private final static String GROUP_ID = ProcessorConstants.getGroupId();
    private final static String AUTO_OFFSET_RESET = ProcessorConstants.getAutoOffsetReset();
    private final static Long CACHE_SIZE = ProcessorConstants.getCacheSize();
    private final static Long CACHE_INIT_RECORDS = ProcessorConstants.getCacheInitRecords();
    private final static Long CACHE_STAT_LOG_AFTER = ProcessorConstants.getCacheStatLogAfter();

    // components
    private final MessageEventProducer messageEventProducer;
    private final MessageEventConsumer messageEventConsumer;
    private final BatchEventProducer batchEventProducer;
    private final BatchEventConsumer batchEventConsumer;
    private final InventoryHandler inventoryHandler;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryEventConsumer inventoryEventConsumer;
    private final MessageHandler messageHandler;
    private final BatchHandler batchHandler;
    private final KafkaConsumer consumer;

    // construct components
    public Processor() {
        // message producer
        this.messageHandler = new MessageHandler();
        ArrayBlockingQueue<MessageEvent> messageEventArrayBlockingQueue = new ArrayBlockingQueue<>(1 << 12);
//        this.messageEventProducer = new MessageEventProducer(new MessageRingBuffer(
//                new MessageEventFactory(),
//                1 << 14,
//                new MessageEventConsumer(this.messageHandler),
//                new ClearEventConsumer<>()
//        ).getRingBuffer());
        this.messageEventProducer = new MessageEventProducer(messageEventArrayBlockingQueue);
        this.messageEventConsumer = new MessageEventConsumer(messageEventArrayBlockingQueue, messageHandler);

        // batch producer
        this.batchHandler = new BatchHandler();
        ArrayBlockingQueue<BatchEvent> batchEventArrayBlockingQueue = new ArrayBlockingQueue<>(1 << 13);
//        this.batchEventProducer = new BatchEventProducer(new BatchRingBuffer(
//                new BatchEventFactory(),
//                1 << 14,
//                new BatchEventConsumer(this.batchHandler),
//                new ClearEventConsumer<>()
//        ).getRingBuffer());
        this.batchEventProducer = new BatchEventProducer(batchEventArrayBlockingQueue);
        this.batchEventConsumer = new BatchEventConsumer(batchEventArrayBlockingQueue, batchHandler);

        // create an inventory handler
        this.inventoryHandler = new InventoryHandler(
                CACHE_SIZE,
                this.messageEventProducer,
                this.batchEventProducer);
        ArrayBlockingQueue<InventoryEvent> inventoryEventArrayBlockingQueue = new ArrayBlockingQueue<>(1 << 13);
        // inventory producer
//        this.inventoryEventProducer = new InventoryEventProducer(new InventoryRingBuffer(
//                new InventoryEventFactory(),
//                1 << 14,
//                new InventoryEventConsumer(inventoryHandler),
//                new ClearEventConsumer<>()
//        ).getRingBuffer());
        this.inventoryEventProducer = new InventoryEventProducer(inventoryEventArrayBlockingQueue);
        this.inventoryEventConsumer = new InventoryEventConsumer(inventoryEventArrayBlockingQueue, inventoryHandler);

        // kafka consumer config
        KafkaConsumerConfig config = new KafkaConsumerConfig(
                KAFKA_BROKER,
                DESERIALIZER_CLASS_CONFIG,
                DESERIALIZER_CLASS_CONFIG,
                AUTO_OFFSET_RESET,
                false,
                GROUP_ID,
                KAFKA_TOPIC,
                0
        );

        this.consumer = new KafkaConsumer(config, inventoryEventProducer);
    }

    public void start() {
        // get max offset from processor.component.database
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();
        Connection databaseConnection = databaseConnector.databaseConnect();
        DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);
        Long maxOffset = databaseQueryExecutor.getMaxOffset("MaxOffset");
        databaseQueryExecutor.close();
        // inventory start action
        this.inventoryHandler.start(CACHE_INIT_RECORDS, CACHE_STAT_LOG_AFTER);
        // start queue consumer
        Thread messageEventThread = new Thread(this.messageEventConsumer);
        Thread batchEventThread = new Thread(this.batchEventConsumer);
        Thread inventoryEventThread = new Thread(this.inventoryEventConsumer);

        messageEventThread.start();
        batchEventThread.start();
        inventoryEventThread.start();
        // run consumer
        this.consumer.run(maxOffset, Duration.ofMillis(100));
    }

    public void stop() {
        // clear components
        this.consumer.close();
        this.inventoryHandler.close();
        this.batchHandler.close();
        this.messageHandler.close();
    }
}
