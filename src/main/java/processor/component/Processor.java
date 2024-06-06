package processor.component;

import processor.component.database.DatabaseConnector;
import processor.component.database.DatabaseQueryExecutor;
import processor.component.disruptor.consumer.BatchEventConsumer;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.InventoryEventConsumer;
import processor.component.disruptor.consumer.MessageEventConsumer;
import processor.component.disruptor.event.batch.BatchEventFactory;
import processor.component.disruptor.event.inventory.InventoryEventFactory;
import processor.component.disruptor.event.message.MessageEventFactory;
import processor.component.disruptor.producer.BatchEventProducer;
import processor.component.disruptor.producer.InventoryEventProducer;
import processor.component.disruptor.producer.MessageEventProducer;
import processor.component.disruptor.ringbuffer.BatchRingBuffer;
import processor.component.disruptor.ringbuffer.InventoryRingBuffer;
import processor.component.disruptor.ringbuffer.MessageRingBuffer;
import processor.component.handler.batch.BatchHandler;
import processor.component.handler.inventory.InventoryHandler;
import processor.component.handler.message.MessageHandler;
import processor.component.kafka.consumer.KafkaConsumer;
import processor.component.kafka.consumer.KafkaConsumerConfig;

import java.sql.Connection;
import java.time.Duration;

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
    private final BatchEventProducer batchEventProducer;
    private final InventoryHandler inventoryHandler;
    private final InventoryEventProducer inventoryEventProducer;
    private final MessageHandler messageHandler;
    private final BatchHandler batchHandler;
    private final KafkaConsumer consumer;

    // construct components
    public Processor() {
        // message producer
        this.messageHandler = new MessageHandler();
        this.messageEventProducer = new MessageEventProducer(new MessageRingBuffer(
                new MessageEventFactory(),
                4096,
                new MessageEventConsumer(this.messageHandler),
                new ClearEventConsumer<>()
        ).getRingBuffer());

        // batch producer
        this.batchHandler = new BatchHandler();
        this.batchEventProducer = new BatchEventProducer(new BatchRingBuffer(
                new BatchEventFactory(),
                4096,
                new BatchEventConsumer(this.batchHandler),
                new ClearEventConsumer<>()
        ).getRingBuffer());

        // create an inventory handler
        this.inventoryHandler = new InventoryHandler(
                CACHE_SIZE,
                this.messageEventProducer,
                this.batchEventProducer);

        // inventory producer
        this.inventoryEventProducer = new InventoryEventProducer(new InventoryRingBuffer(
                new InventoryEventFactory(),
                8192,
                new InventoryEventConsumer(inventoryHandler),
                new ClearEventConsumer<>()
        ).getRingBuffer());

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
        // run consumer
        this.consumer.run(maxOffset, Duration.ofMillis(100));
    }

    public void stop() {
        // clear components
        this.consumer.close();
        this.inventoryEventProducer.close();
        this.inventoryHandler.close();
        this.batchEventProducer.close();
        this.batchHandler.close();
        this.messageEventProducer.close();
        this.messageHandler.close();
    }
}
