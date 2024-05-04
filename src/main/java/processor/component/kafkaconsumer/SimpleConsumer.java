package processor.component.kafkaconsumer;


import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;

public class SimpleConsumer {

    public static void main(String[] args) {
        KafkaConsumerConfig config = new KafkaConsumerConfig(
                "localhost:9092",
                StringDeserializer.class.getName(),
                StringDeserializer.class.getName(),
                "earliest",
                false,
                "invensys",
                0
        );

        KafkaConsumer kafkaConsumer = new KafkaConsumer(config);

        kafkaConsumer.run(0L, Duration.ofMillis(100));
    }
}

