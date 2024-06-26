package processor.component;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import processor.component.disruptor.producer.BatchEventProducer;
import processor.component.disruptor.producer.InventoryEventProducer;
import processor.component.disruptor.producer.MessageEventProducer;
import processor.component.handler.batch.BatchHandler;
import processor.component.handler.inventory.InventoryHandler;
import processor.component.handler.message.MessageHandler;
import processor.component.kafka.consumer.KafkaConsumer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProcessorTest {

    @Mock
    private MessageEventProducer mockMessageEventProducer;

    @Mock
    private BatchEventProducer mockBatchEventProducer;

    @Mock
    private InventoryHandler mockInventoryHandler;

    @Mock
    private InventoryEventProducer mockInventoryEventProducer;

    @Mock
    private MessageHandler mockMessageHandler;

    @Mock
    private BatchHandler mockBatchHandler;

    @Mock
    private KafkaConsumer mockConsumer;

    @Mock
    private DatabaseConnector mockDatabaseConnector;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseQueryExecutor mockDatabaseQueryExecutor;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private Processor processor;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<DatabaseConnector> mockedConnector = mockStatic(DatabaseConnector.class)) {
            mockedConnector.when(DatabaseConnector::databaseConnectorFactory).thenReturn(mockDatabaseConnector);
            when(mockDatabaseConnector.databaseConnect()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong(anyString())).thenReturn(100L);

            try (MockedStatic<ProcessorConstants> mockedConstants = mockStatic(ProcessorConstants.class)) {
                mockedConstants.when(ProcessorConstants::getKafkaBroker).thenReturn("localhost:9092");
                mockedConstants.when(ProcessorConstants::getKafkaTopic).thenReturn("topic");
                mockedConstants.when(ProcessorConstants::getDeserializerClassConfig).thenReturn(StringDeserializer.class.getName());
                mockedConstants.when(ProcessorConstants::getGroupId).thenReturn("groupId");
                mockedConstants.when(ProcessorConstants::getAutoOffsetReset).thenReturn("earliest");
                mockedConstants.when(ProcessorConstants::getCacheSize).thenReturn(1000L);
                mockedConstants.when(ProcessorConstants::getCacheInitRecords).thenReturn(100L);
                mockedConstants.when(ProcessorConstants::getCacheStatLogAfter).thenReturn(10L);

                processor = new Processor();

                // Inject the mocks using reflection
                injectMocksIntoProcessor();
            }
        }
    }

    private void injectMocksIntoProcessor() {
        try {
            var fieldConsumer = Processor.class.getDeclaredField("consumer");
            fieldConsumer.setAccessible(true);
            fieldConsumer.set(processor, mockConsumer);

            var fieldMessageEventProducer = Processor.class.getDeclaredField("messageEventProducer");
            fieldMessageEventProducer.setAccessible(true);
            fieldMessageEventProducer.set(processor, mockMessageEventProducer);

            var fieldBatchEventProducer = Processor.class.getDeclaredField("batchEventProducer");
            fieldBatchEventProducer.setAccessible(true);
            fieldBatchEventProducer.set(processor, mockBatchEventProducer);

            var fieldInventoryHandler = Processor.class.getDeclaredField("inventoryHandler");
            fieldInventoryHandler.setAccessible(true);
            fieldInventoryHandler.set(processor, mockInventoryHandler);

            var fieldInventoryEventProducer = Processor.class.getDeclaredField("inventoryEventProducer");
            fieldInventoryEventProducer.setAccessible(true);
            fieldInventoryEventProducer.set(processor, mockInventoryEventProducer);

            var fieldMessageHandler = Processor.class.getDeclaredField("messageHandler");
            fieldMessageHandler.setAccessible(true);
            fieldMessageHandler.set(processor, mockMessageHandler);

            var fieldBatchHandler = Processor.class.getDeclaredField("batchHandler");
            fieldBatchHandler.setAccessible(true);
            fieldBatchHandler.set(processor, mockBatchHandler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructor() {
        assertNotNull(processor);
    }

    @Test
    void testStart() {
        processor.start();

        verify(mockInventoryHandler).start(anyLong(), anyLong());
        verify(mockConsumer).run(any(), any(Duration.class));
    }

    @Test
    void testStop() {
        processor.stop();

        verify(mockConsumer).close();
        verify(mockInventoryEventProducer).close();
        verify(mockInventoryHandler).close();
        verify(mockBatchEventProducer).close();
        verify(mockBatchHandler).close();
        verify(mockMessageEventProducer).close();
        verify(mockMessageHandler).close();
    }
}
