package processor.component.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KafkaConsumerConfigTest {

    @Test
    void testKafkaConsumerConfigWithAllProperties() {
        String broker = "localhost:9092";
        String keyDeserializerClass = "org.apache.kafka.common.serialization.StringDeserializer";
        String valueDeserializerClass = "org.apache.kafka.common.serialization.StringDeserializer";
        String autoOffsetReset = "earliest";
        Boolean enableAutoCommit = true;
        String groupId = "test-group";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaConsumerConfig config = new KafkaConsumerConfig(broker, keyDeserializerClass, valueDeserializerClass, autoOffsetReset, enableAutoCommit, groupId, topic, partition);

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(keyDeserializerClass, props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(valueDeserializerClass, props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertEquals(autoOffsetReset, props.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(enableAutoCommit.toString(), props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals(groupId, props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());
    }

    @Test
    void testKafkaConsumerConfigWithNullValues() {
        String broker = "localhost:9092";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaConsumerConfig config = new KafkaConsumerConfig(broker, null, null, null, null, null, topic, partition);

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertNull(props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());
    }

    @Test
    void testKafkaConsumerConfigGetters() {
        String broker = "localhost:9092";
        String keyDeserializerClass = "org.apache.kafka.common.serialization.StringDeserializer";
        String valueDeserializerClass = "org.apache.kafka.common.serialization.StringDeserializer";
        String autoOffsetReset = "earliest";
        Boolean enableAutoCommit = true;
        String groupId = "test-group";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaConsumerConfig config = new KafkaConsumerConfig(broker, keyDeserializerClass, valueDeserializerClass, autoOffsetReset, enableAutoCommit, groupId, topic, partition);

        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(keyDeserializerClass, props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(valueDeserializerClass, props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertEquals(autoOffsetReset, props.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(enableAutoCommit.toString(), props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals(groupId, props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
    }

    @Test
    void testAddPropertyIfNotNull() throws Exception {
        KafkaConsumerConfig config = new KafkaConsumerConfig(
                "broker",
                "keyDeserializer",
                "valueDeserializer",
                "earliest",
                true,
                "test-group",
                "test-topic",
                1);

        Method method = KafkaConsumerConfig.class.getDeclaredMethod("addPropertyIfNotNull", Properties.class, String.class, Object.class);
        method.setAccessible(true);

        Properties propsWithNonNullValue = new Properties();
        method.invoke(config, propsWithNonNullValue, "testKey", "testValue");
        assertEquals("testValue", propsWithNonNullValue.getProperty("testKey"));

        Properties propsWithNullValue = new Properties();
        method.invoke(config, propsWithNullValue, "nullKey", null);
        assertNull(propsWithNullValue.getProperty("nullKey"));
    }
}
