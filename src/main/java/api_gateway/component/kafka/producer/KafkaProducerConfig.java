package api_gateway.component.kafka.producer;

import lombok.Data;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

// This project will work with a topic with single partition
@Data
@Getter
public class KafkaProducerConfig {
    private final String topic;
    private final Properties kafkaProps;
    private final Integer partition;

    public KafkaProducerConfig(String broker, String keySerializerClass, String valueSerializerClass, String acksConfig, String topic, Integer partition) {
        var props = new Properties();
        addPropertyIfNotNull(props, ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        addPropertyIfNotNull(props, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        addPropertyIfNotNull(props, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
        addPropertyIfNotNull(props, ProducerConfig.ACKS_CONFIG, acksConfig);
        this.kafkaProps = props;
        this.topic = topic;
        this.partition = partition;
    }

    private static <T> void addPropertyIfNotNull(Properties props, String key, T value) {
        if (value != null) {
            // Kafka expects all properties as String
            props.put(key, value.toString());
        }
    }
}
