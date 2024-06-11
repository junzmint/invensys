package processor.component.kafka.consumer;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

// This project will work with a topic with single partition
@Getter
public class KafkaConsumerConfig {
    private final String topic;
    private final Properties kafkaProps;
    private final Integer partition;

    public KafkaConsumerConfig(String broker, String keyDeserializerClass, String valueDeserializerClass, String autoOffsetReset, Boolean enableAutoCommit, String groupId, String topic, Integer partition) {
        var props = new Properties();
        addPropertyIfNotNull(props, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        addPropertyIfNotNull(props, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
        addPropertyIfNotNull(props, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);
        addPropertyIfNotNull(props, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        addPropertyIfNotNull(props, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        addPropertyIfNotNull(props, ConsumerConfig.GROUP_ID_CONFIG, groupId);
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
