package processor.component.kafka.consumer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import processor.component.disruptor.producer.InventoryEventProducer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaConsumerTest {

    @Mock
    private InventoryEventProducer mockInventoryEventProducer;

    @Mock
    private org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> mockKafkaConsumer;

    private KafkaConsumer kafkaConsumer;

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        config = new KafkaConsumerConfig("localhost:9092", StringDeserializer.class.getName(), StringDeserializer.class.getName(), "latest", true, "groupId", "topic", 0);
        kafkaConsumer = new KafkaConsumer(config, mockInventoryEventProducer);
        injectMockKafkaConsumer();
    }

    private void injectMockKafkaConsumer() {
        try {
            Field kafkaConsumerField = KafkaConsumer.class.getDeclaredField("kafkaConsumer");
            kafkaConsumerField.setAccessible(true);
            kafkaConsumerField.set(kafkaConsumer, mockKafkaConsumer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        kafkaConsumer.close();
    }

    @Test
    void testConstructor() {
        KafkaConsumer kafkaConsumer = new KafkaConsumer(config, mockInventoryEventProducer);
        assertNotNull(kafkaConsumer);
    }

    @Test
    void testConstructorWithNullConfig() {
        InventoryEventProducer mockInventoryEventProducer = mock(InventoryEventProducer.class);

        Constructor<KafkaConsumer> constructor;
        try {
            constructor = KafkaConsumer.class.getDeclaredConstructor(KafkaConsumerConfig.class, InventoryEventProducer.class);
            constructor.setAccessible(true);
            Exception exception = assertThrows(InvocationTargetException.class, () -> constructor.newInstance(null, mockInventoryEventProducer));
            assertTrue(exception.getCause() instanceof NullPointerException);
        } catch (NoSuchMethodException e) {
            fail(e);
        }
    }

    @Test
    void testRun() {
        doNothing().when(mockKafkaConsumer).assign(any());
        doNothing().when(mockKafkaConsumer).seek(any(), anyLong());

        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        Header header = new RecordHeader(KafkaConsumer.IS_VALUE, "true".getBytes());
        record.headers().add(header);

        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(Collections.singletonMap(new TopicPartition("topic", 0), List.of(record)));
        ConsumerRecords<Object, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(mockKafkaConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(records)
                .thenReturn(emptyRecords);

        Thread consumerThread = new Thread(() -> kafkaConsumer.run(0L, Duration.ofMillis(100)));
        consumerThread.start();

        try {
            Thread.sleep(200);
            consumerThread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kafkaConsumer.close();
        verify(mockInventoryEventProducer, atLeastOnce()).onData(anyLong(), any(Message.class));
        verify(mockKafkaConsumer, times(1)).close();
    }

    @Test
    void testRunWithInitialInterruptedThread() {
        doNothing().when(mockKafkaConsumer).assign(any());
        doNothing().when(mockKafkaConsumer).seek(any(), anyLong());

        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        Header header = new RecordHeader(KafkaConsumer.IS_VALUE, "true".getBytes());
        record.headers().add(header);

        ConsumerRecords<Object, Object> records = new ConsumerRecords<>(Collections.singletonMap(new TopicPartition("topic", 0), List.of(record)));
        ConsumerRecords<Object, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(mockKafkaConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(records)
                .thenReturn(emptyRecords);

        Thread consumerThread = new Thread(() -> {
            Thread.currentThread().interrupt();  // Interrupt the thread right at the start
            kafkaConsumer.run(0L, Duration.ofMillis(100));
        });

        consumerThread.start();
        kafkaConsumer.close();
        verify(mockKafkaConsumer, times(1)).close();
    }


    @Test
    void testClose() {
        kafkaConsumer.close();
        verify(mockKafkaConsumer, times(1)).close();
    }

    @Test
    void testOnException() {
        Throwable mockException = new KafkaException("Test Exception");

        try (MockedStatic<KafkaConsumerLogger> ignored = mockStatic(KafkaConsumerLogger.class)) {
            doThrow(mockException).when(mockKafkaConsumer).poll(any(Duration.class));

            Thread consumerThread = new Thread(() -> kafkaConsumer.run(0L, Duration.ofMillis(100)));
            consumerThread.start();

            try {
                Thread.sleep(200);
                consumerThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // mockedLogger.verify(() -> KafkaConsumerLogger.logProcessorError(anyString(), any(Throwable.class)), times(1));
        }
    }

    @Test
    void testBuildMessage() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        Header header = new RecordHeader(KafkaConsumer.IS_VALUE, "true".getBytes());
        record.headers().add(header);

        Method buildMessageMethod = KafkaConsumer.class.getDeclaredMethod("buildMessage", ConsumerRecord.class);
        buildMessageMethod.setAccessible(true);
        Message message = (Message) buildMessageMethod.invoke(kafkaConsumer, record);

        assertNotNull(message);
        BObject headers = message.headers();
        assertNotNull(headers);
        assertEquals("key", headers.getString(KafkaConsumer.KEY));
        assertEquals(0L, headers.getLong(KafkaConsumer.OFFSET));
        assertEquals("topic", headers.getString(KafkaConsumer.TOPIC));
        assertEquals(0, headers.getInteger(KafkaConsumer.PARTITION));
    }

    @Test
    void testBuildMessageWithNonNullKeyAndIsValueHeader() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        Header header = new RecordHeader(KafkaConsumer.IS_VALUE, "true".getBytes());
        record.headers().add(header);

        Method buildMessageMethod = KafkaConsumer.class.getDeclaredMethod("buildMessage", ConsumerRecord.class);
        buildMessageMethod.setAccessible(true);
        Message message = (Message) buildMessageMethod.invoke(kafkaConsumer, record);

        assertNotNull(message);
        BObject headers = message.headers();
        assertNotNull(headers);
        assertEquals("key", headers.getString(KafkaConsumer.KEY));
        assertEquals(0L, headers.getLong(KafkaConsumer.OFFSET));
        assertEquals("topic", headers.getString(KafkaConsumer.TOPIC));
        assertEquals(0, headers.getInteger(KafkaConsumer.PARTITION));
        assertNotNull(message.body());
    }

    @Test
    void testBuildMessageWithNullKeyAndNonIsValueHeader() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, null, "value");
        Header header = new RecordHeader("someOtherHeader", "true".getBytes());
        record.headers().add(header);

        Method buildMessageMethod = KafkaConsumer.class.getDeclaredMethod("buildMessage", ConsumerRecord.class);
        buildMessageMethod.setAccessible(true);
        Message message = (Message) buildMessageMethod.invoke(kafkaConsumer, record);

        assertNotNull(message);
        BObject headers = message.headers();
        assertNotNull(headers);
        assertEquals(0L, headers.getLong(KafkaConsumer.OFFSET));
        assertEquals("topic", headers.getString(KafkaConsumer.TOPIC));
        assertEquals(0, headers.getInteger(KafkaConsumer.PARTITION));
        assertNotNull(message.body());
    }

    @Test
    void testBuildMessageWithNonNullKeyAndNonIsValueHeader() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");
        Header header = new RecordHeader("someOtherHeader", "true".getBytes());
        record.headers().add(header);

        Method buildMessageMethod = KafkaConsumer.class.getDeclaredMethod("buildMessage", ConsumerRecord.class);
        buildMessageMethod.setAccessible(true);
        Message message = (Message) buildMessageMethod.invoke(kafkaConsumer, record);

        assertNotNull(message);
        BObject headers = message.headers();
        assertNotNull(headers);
        assertEquals("key", headers.getString(KafkaConsumer.KEY));
        assertEquals(0L, headers.getLong(KafkaConsumer.OFFSET));
        assertEquals("topic", headers.getString(KafkaConsumer.TOPIC));
        assertEquals(0, headers.getInteger(KafkaConsumer.PARTITION));
        assertNotNull(message.body());
    }

    @Test
    void testBuildMessageWithNullKeyAndIsValueHeader() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, null, "value");
        Header header = new RecordHeader(KafkaConsumer.IS_VALUE, "true".getBytes());
        record.headers().add(header);

        Method buildMessageMethod = KafkaConsumer.class.getDeclaredMethod("buildMessage", ConsumerRecord.class);
        buildMessageMethod.setAccessible(true);
        Message message = (Message) buildMessageMethod.invoke(kafkaConsumer, record);

        assertNotNull(message);
        BObject headers = message.headers();
        assertNotNull(headers);
        assertEquals(0L, headers.getLong(KafkaConsumer.OFFSET));
        assertEquals("topic", headers.getString(KafkaConsumer.TOPIC));
        assertEquals(0, headers.getInteger(KafkaConsumer.PARTITION));
        assertNotNull(message.body());
    }

    @Test
    void testDeserializeWithFormat_byteArray() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, null, new byte[]{1, 2, 3});

        Method method = KafkaConsumer.class.getDeclaredMethod("deserializeWithFormat", ConsumerRecord.class);
        method.setAccessible(true);
        BElement result = (BElement) method.invoke(kafkaConsumer, record);

        assertNotNull(result);
    }

    @Test
    void testDeserializeWithFormat_string() throws Exception {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("topic", 0, 0L, null, "testValue");

        Method method = KafkaConsumer.class.getDeclaredMethod("deserializeWithFormat", ConsumerRecord.class);
        method.setAccessible(true);
        BElement result = (BElement) method.invoke(kafkaConsumer, record);

        assertNotNull(result);
    }

    @Test
    void testDeserialize_nullResponseBody() throws Exception {
        Method method = KafkaConsumer.class.getDeclaredMethod("deserialize", byte[].class);
        method.setAccessible(true);
        BElement result = (BElement) method.invoke(kafkaConsumer, (Object) null);

        assertNull(result);
    }

    @Test
    void testDeserialize_emptyResponseBody() throws Exception {
        Method method = KafkaConsumer.class.getDeclaredMethod("deserialize", byte[].class);
        method.setAccessible(true);
        BElement result = (BElement) method.invoke(kafkaConsumer, new byte[0]);

        assertNull(result);
    }

    @Test
    void testDeserialize_nonEmptyResponseBody() throws Exception {
        byte[] responseBody = new byte[]{1, 2, 3};

        Method method = KafkaConsumer.class.getDeclaredMethod("deserialize", byte[].class);
        method.setAccessible(true);
        BElement result = (BElement) method.invoke(kafkaConsumer, responseBody);

        assertNotNull(result);
    }
}
