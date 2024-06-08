package api_gateway.component.kafka.producer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.joo.promise4j.Deferred;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KafkaProducerTest {

    private KafkaProducerConfig config;
    private KafkaProducer kafkaProducer;
    private org.apache.kafka.clients.producer.KafkaProducer<Object, Object> mockKafkaProducer;
    private Deferred<Message, Exception> mockDeferred;

    @BeforeEach
    void setUp() {
        config = new KafkaProducerConfig("localhost:9092",
                "org.apache.kafka.common.serialization.StringSerializer",
                "org.apache.kafka.common.serialization.StringSerializer",
                "all",
                "test-topic",
                0);

        mockKafkaProducer = mock(org.apache.kafka.clients.producer.KafkaProducer.class);
        mockDeferred = mock(Deferred.class);

        kafkaProducer = new KafkaProducer(config);
        kafkaProducer.setKafkaProducer(mockKafkaProducer);
    }

    @Test
    void testProduceWithNullValue() {
        Message message = Message.ofAny(null);
        kafkaProducer.produce(message, mockDeferred, "key", true);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockDeferred).resolve(messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        assertEquals("BODY_NULL_VALUE", capturedMessage.body().asValue().getString());
        verifyNoInteractions(mockKafkaProducer);
    }

    @Test
    void testProduceWithValidMessage() throws Exception {
        Message message = Message.ofAny(BValue.of("value"));
        doAnswer(invocation -> {
            ProducerRecord<Object, Object> record = invocation.getArgument(0);
            assertEquals("test-topic", record.topic());
            assertEquals(0, record.partition());
            assertEquals("key", record.key());
            assertEquals("value", record.value());
            return null;
        }).when(mockKafkaProducer).send(any(ProducerRecord.class), any());

        kafkaProducer.produce(message, mockDeferred, "key", false);
        verify(mockKafkaProducer).send(any(ProducerRecord.class), any());
    }

    @Test
    void testOnProduceWithAck() throws Exception {
        var topicPartition = new org.apache.kafka.common.TopicPartition("test-topic", 0);
        RecordMetadata metadata = new RecordMetadata(topicPartition, 0, 0, 0L, Long.valueOf(0L), 0, 0);
        kafkaProducer = spy(kafkaProducer);

        // Access private method using reflection
        Method onProduceMethod = KafkaProducer.class.getDeclaredMethod("onProduce", Boolean.class, Deferred.class, RecordMetadata.class, Exception.class);
        onProduceMethod.setAccessible(true);

        // Call the private method using reflection
        onProduceMethod.invoke(kafkaProducer, true, mockDeferred, metadata, null);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockDeferred).resolve(messageCaptor.capture());

        Message capturedMessage = messageCaptor.getValue();
        BObject headers = capturedMessage.headers();
        assertEquals(true, headers.getBoolean("kafka.IS_ACK_MSG"));
        assertEquals(metadata.timestamp(), headers.getLong("kafka.TIMESTAMP"));
        assertEquals(metadata.offset(), headers.getLong("kafka.OFFSET"));
        assertEquals(metadata.partition(), headers.getInteger("kafka.PARTITION"));
        assertEquals(metadata.topic(), headers.getString("kafka.TOPIC"));
    }

    @Test
    void testOnProduceWithoutAck() throws Exception {
        kafkaProducer = spy(kafkaProducer);

        // Access private method using reflection
        Method onProduceMethod = KafkaProducer.class.getDeclaredMethod("onProduce", Boolean.class, Deferred.class, RecordMetadata.class, Exception.class);
        onProduceMethod.setAccessible(true);

        // Call the private method using reflection
        onProduceMethod.invoke(kafkaProducer, false, mockDeferred, null, new Exception("Test exception"));

        verify(mockDeferred).resolve(any(Message.class));
    }

    @Test
    void testBuildProducerRecord() throws Exception {
        Message message = Message.ofAny(BElement.ofAny("value"));
        message.headers().setAny("headerKey", "keyValue");

        // Access private method using reflection
        Method buildProducerRecordMethod = KafkaProducer.class.getDeclaredMethod("buildProducerRecord", String.class, Integer.class, String.class, Message.class);
        buildProducerRecordMethod.setAccessible(true);

        // Call the private method using reflection
        ProducerRecord<Object, Object> record = (ProducerRecord<Object, Object>) buildProducerRecordMethod.invoke(kafkaProducer, "test-topic", 0, "key", message);

        assertEquals("test-topic", record.topic());
        assertEquals(0, record.partition());
        assertEquals("key", record.key());
        assertEquals("value", record.value());
        assertNotNull(record.headers().lastHeader("kafka.IS_VALUE"));
        assertEquals("keyValue", BElement.ofBytes(record.headers().lastHeader("headerKey").value()).asValue().getString());
    }

    @Test
    void testBuildProducerRecordWithNullBody() throws Exception {
        Message message = Message.ofAny(null);

        // Access private method using reflection
        Method buildProducerRecordMethod = KafkaProducer.class.getDeclaredMethod("buildProducerRecord", String.class, Integer.class, String.class, Message.class);
        buildProducerRecordMethod.setAccessible(true);

        // Call the private method using reflection
        ProducerRecord<Object, Object> record = (ProducerRecord<Object, Object>) buildProducerRecordMethod.invoke(kafkaProducer, "test-topic", 0, "key", message);

        assertEquals("test-topic", record.topic());
        assertEquals(0, record.partition());
        assertEquals("key", record.key());
        assertNull(record.value());
        assertNull(record.headers().lastHeader("kafka.IS_VALUE"));
    }

    @Test
    void testConvert() throws Exception {
        BObject body = BObject.of("key", BValue.of("value"));

        // Access private method using reflection
        Method convertMethod = KafkaProducer.class.getDeclaredMethod("convert", BElement.class);
        convertMethod.setAccessible(true);

        // Call the private method using reflection
        Object result = convertMethod.invoke(kafkaProducer, body);

        assertEquals(body.toJson(), result);
    }

    @Test
    void testConvertWithNullBody() throws Exception {
        BElement body = null;

        // Access private method using reflection
        Method convertMethod = KafkaProducer.class.getDeclaredMethod("convert", BElement.class);
        convertMethod.setAccessible(true);

        // Call the private method using reflection
        Object result = convertMethod.invoke(kafkaProducer, body);

        assertNull(result);
    }

    @Test
    void testBuildAckMessage() throws Exception {
        var topicPartition = new org.apache.kafka.common.TopicPartition("test-topic", 0);
        RecordMetadata metadata = new RecordMetadata(topicPartition, 0, 0, 0L, Long.valueOf(0L), 0, 0);

        // Access private method using reflection
        Method buildAckMessageMethod = KafkaProducer.class.getDeclaredMethod("buildAckMessage", RecordMetadata.class);
        buildAckMessageMethod.setAccessible(true);

        // Call the private method using reflection
        Message ackMessage = (Message) buildAckMessageMethod.invoke(kafkaProducer, metadata);

        BObject headers = ackMessage.headers();
        assertEquals(true, headers.getBoolean("kafka.IS_ACK_MSG"));
        assertEquals(metadata.timestamp(), headers.getLong("kafka.TIMESTAMP"));
        assertEquals(metadata.offset(), headers.getLong("kafka.OFFSET"));
        assertEquals(metadata.partition(), headers.getInteger("kafka.PARTITION"));
        assertEquals(metadata.topic(), headers.getString("kafka.TOPIC"));
    }

    @Test
    void testBuildAckMessageWithNullMetadata() throws Exception {
        // Access private method using reflection
        Method buildAckMessageMethod = KafkaProducer.class.getDeclaredMethod("buildAckMessage", RecordMetadata.class);
        buildAckMessageMethod.setAccessible(true);

        // Call the private method using reflection
        Message ackMessage = (Message) buildAckMessageMethod.invoke(kafkaProducer, (RecordMetadata) null);

        assertNull(ackMessage);
    }

    @Test
    void testOnClose() {
        kafkaProducer.onClose();
        verify(mockKafkaProducer).close();
    }
}
