package api_gateway.component.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KafkaProducerConfigTest {

    @Test
    void testKafkaProducerConfigWithAllProperties() {
        String broker = "localhost:9092";
        String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        String valueSerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        String acksConfig = "all";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaProducerConfig config = new KafkaProducerConfig(broker, keySerializerClass, valueSerializerClass, acksConfig, topic, partition);

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(keySerializerClass, props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(valueSerializerClass, props.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(acksConfig, props.getProperty(ProducerConfig.ACKS_CONFIG));
        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());
    }

    @Test
    void testKafkaProducerConfigWithNullValues() {
        String broker = "localhost:9092";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaProducerConfig config = new KafkaProducerConfig(broker, null, null, null, topic, partition);

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertNull(props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertNull(props.getProperty(ProducerConfig.ACKS_CONFIG));
        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());
    }

    @Test
    void testKafkaProducerConfigGetters() {
        String broker = "localhost:9092";
        String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        String valueSerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        String acksConfig = "all";
        String topic = "test-topic";
        Integer partition = 1;

        KafkaProducerConfig config = new KafkaProducerConfig(broker, keySerializerClass, valueSerializerClass, acksConfig, topic, partition);

        assertEquals(topic, config.getTopic());
        assertEquals(partition, config.getPartition());

        Properties props = config.getKafkaProps();
        assertEquals(broker, props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(keySerializerClass, props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(valueSerializerClass, props.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(acksConfig, props.getProperty(ProducerConfig.ACKS_CONFIG));
    }

    @Test
    void testAddPropertyIfNotNull() throws Exception {
        KafkaProducerConfig config = new KafkaProducerConfig("broker", "keySerializer", "valueSerializer", "acks", "topic", 1);

        Method method = KafkaProducerConfig.class.getDeclaredMethod("addPropertyIfNotNull", Properties.class, String.class, Object.class);
        method.setAccessible(true);

        Properties propsWithNonNullValue = new Properties();
        method.invoke(config, propsWithNonNullValue, "testKey", "testValue");
        assertEquals("testValue", propsWithNonNullValue.getProperty("testKey"));

        Properties propsWithNullValue = new Properties();
        method.invoke(config, propsWithNullValue, "nullKey", null);
        assertNull(propsWithNullValue.getProperty("nullKey"));
    }
}
