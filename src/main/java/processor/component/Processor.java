package processor.component;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import processor.component.cache.LocalCache;
import processor.component.disruptor.consumer.BatchEventConsumer;
import processor.component.disruptor.consumer.ClearEventConsumer;
import processor.component.disruptor.consumer.InventoryEventConsumer;
import processor.component.disruptor.consumer.MessageEventConsumer;
import processor.component.disruptor.event.batch.BatchEvent;
import processor.component.disruptor.event.batch.BatchEventFactory;
import processor.component.disruptor.event.inventory.InventoryEventFactory;
import processor.component.disruptor.event.message.MessageEvent;
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
import java.util.concurrent.TimeUnit;

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
    private final DatabaseQueryExecutor databaseQueryExecutor;
    private final MessageEventProducer messageEventProducer;
    private final BatchEventProducer batchEventProducer;
    private final LocalCache localCache;
    private final InventoryHandler inventoryHandler;
    private final InventoryEventProducer inventoryEventProducer;
    private final MessageHandler messageHandler;
    private final BatchHandler batchHandler;
    private final KafkaConsumer consumer;
    // offset we will consume from after start/restart
    private final Long maxOffset;

    // construct components
    public Processor() {
        // connect to database and create a query executor instance
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();
        Connection databaseConnection = databaseConnector.databaseConnect();
        databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        // message producer
        this.messageHandler = new MessageHandler();
        this.messageEventProducer = new MessageEventProducer(new MessageRingBuffer(
                new MessageEventFactory(),
                2048,
                new MessageEventConsumer(this.messageHandler),
                new ClearEventConsumer<MessageEvent>()
        ).getRingBuffer());

        // batch producer
        this.batchHandler = new BatchHandler(databaseQueryExecutor);
        this.batchEventProducer = new BatchEventProducer(new BatchRingBuffer(
                new BatchEventFactory(),
                2048,
                new BatchEventConsumer(this.batchHandler),
                new ClearEventConsumer<BatchEvent>()
        ).getRingBuffer());

        // create a local cache for in memory processing
        this.localCache = new LocalCache(databaseQueryExecutor, CACHE_SIZE);
        localCache.initCache(CACHE_INIT_RECORDS);

        // create an inventory handler
        this.inventoryHandler = new InventoryHandler(
                this.localCache,
                this.messageEventProducer,
                this.batchEventProducer);

        // inventory producer
        this.inventoryEventProducer = new InventoryEventProducer(new InventoryRingBuffer(
                new InventoryEventFactory(),
                4096,
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

        // get max offset from database
        this.maxOffset = databaseQueryExecutor.getMaxOffset("MaxOffset");
    }

    public void start() {
        // schedule log for local cache
        this.localCache.scheduleCacheLogging(CACHE_STAT_LOG_AFTER, TimeUnit.SECONDS);
        // run consumer
        this.consumer.run(this.maxOffset, Duration.ofMillis(100));
    }

    public void stop() {
        // close components
        this.consumer.onClose();
        this.inventoryEventProducer.close();
        this.inventoryHandler.close();
        this.localCache.onStop();
        this.batchEventProducer.close();
        this.batchHandler.close();
        this.messageEventProducer.close();
        this.messageHandler.close();
        this.databaseQueryExecutor.close();
    }
}
