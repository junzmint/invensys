package processor.component.kafka.consumer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import lombok.NonNull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import processor.component.queue.producer.InventoryEventProducer;

import java.time.Duration;
import java.util.List;

public class KafkaConsumer {
    public static final String KEY = "kafka.KEY";
    public static final String PARTITION = "kafka.PARTITION";
    public static final String TOPIC = "kafka.TOPIC";
    public static final String OFFSET = "kafka.OFFSET";
    public static final String TIMESTAMP = "kafka.TIMESTAMP";
    public static final String IS_VALUE = "kafka.IS_VALUE";

    private final String topic;
    private final Integer partition;
    private final org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer;

    private final InventoryEventProducer inventoryEventProducer;

    public KafkaConsumer(final @NonNull KafkaConsumerConfig config, InventoryEventProducer inventoryEventProducer) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.kafkaConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(config.getKafkaProps());
        this.inventoryEventProducer = inventoryEventProducer;
    }

    public void run(Long offSet, Duration pollDuration) {
        try {
            if (!Thread.currentThread().isInterrupted()) {
                onRun(offSet, pollDuration);
            }
        } catch (KafkaException exception) {
            KafkaConsumerLogger.logProcessorError("KAFKA_EXCEPTION_CONSUMER_THREAD", exception);
        } catch (Exception exception) {
            KafkaConsumerLogger.logProcessorError("EXCEPTION_CONSUMER_THREAD", exception);
        } finally {
            close();
        }
    }

    // consumer poll message
    private void onRun(Long offset, Duration pollDuration) {
        // assign MaxOffset we will consume from
        long maxOffset = offset + 1;
        // assign topic
        TopicPartition partitionToReadFrom = new TopicPartition(this.topic, this.partition);
        this.kafkaConsumer.assign(List.of(partitionToReadFrom));
        // seek
        this.kafkaConsumer.seek(partitionToReadFrom, maxOffset);

        while (true) {
            ConsumerRecords<Object, Object> records = this.kafkaConsumer.poll(pollDuration);
            for (ConsumerRecord<Object, Object> record : records) {
                Message message = buildMessage(record);
                maxOffset = record.offset();
                this.inventoryEventProducer.onData(maxOffset, message);
                // KafkaConsumerLogger.logKafkaConsumerInfo("KAFKA_CONSUMED_OFFSET: " + maxOffset);
            }
        }
    }

    // build message from consumer record
    private Message buildMessage(ConsumerRecord<Object, Object> record) {
        var headers = BObject.ofEmpty();

        populateCommonHeaders(headers, record);

        headers.putAny(OFFSET, record.offset());
        if (record.key() != null) {
            headers.putAny(KEY, record.key());
        }

        var isValue = false;
        for (Header header : record.headers()) {
            headers.putAny(header.key(), header.value());
            if (IS_VALUE.equals(header.key()))
                isValue = true;
        }

        var body = isValue ? BElement.ofJson(record.value().toString()) : deserializeWithFormat(record);
        return Message.ofAny(headers, body);
    }

    private void populateCommonHeaders(BObject headers, ConsumerRecord<Object, Object> record) {
        headers.putAny(PARTITION, record.partition());
        headers.putAny(TOPIC, record.topic());
        headers.putAny(TIMESTAMP, record.timestamp());
    }

    private BElement deserializeWithFormat(ConsumerRecord<Object, Object> record) {
        var value = record.value();
        return deserialize(value instanceof byte[] ? (byte[]) value : value.toString().getBytes());
    }

    private BElement deserialize(byte[] responseBody) {
        if (responseBody == null || responseBody.length == 0)
            return null;
        return BElement.ofBytes(responseBody);
    }

    public void close() {
        this.kafkaConsumer.close();
    }
}
