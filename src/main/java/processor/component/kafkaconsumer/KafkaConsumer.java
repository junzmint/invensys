package processor.component.kafkaconsumer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import lombok.NonNull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import utils.logging.LoggerUtil;

import java.time.Duration;
import java.util.List;

public class KafkaConsumer {
    private final String topic;
    private final Integer partition;
    private final org.apache.kafka.clients.consumer.KafkaConsumer<Object, Object> kafkaConsumer;

    public KafkaConsumer(final @NonNull KafkaConsumerConfig config) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.kafkaConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(config.getKafkaProps());
    }

    // build message from consumer record
    private Message buildMessage(ConsumerRecord<Object, Object> record) {
        var headers = BObject.ofEmpty();

        populateCommonHeaders(headers, record);

        headers.putAny(KafkaConsumerConstants.OFFSET, record.offset());
        if (record.key() != null) {
            headers.putAny(KafkaConsumerConstants.KEY, record.key());
        }

        var isValue = false;
        for (Header header : record.headers()) {
            headers.putAny(header.key(), header.value());
            if (KafkaConsumerConstants.IS_VALUE.equals(header.key()))
                isValue = true;
        }

        var body = isValue ? BElement.ofAny(record.value()) : deserializeWithFormat(record);
        return Message.ofAny(headers, body);
    }

    private void populateCommonHeaders(BObject headers, ConsumerRecord<Object, Object> record) {
        headers.putAny(KafkaConsumerConstants.PARTITION, record.partition());
        headers.putAny(KafkaConsumerConstants.TOPIC, record.topic());
        headers.putAny(KafkaConsumerConstants.TIMESTAMP, record.timestamp());
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

    // consumer poll message
    private void onRun(Long offSet, Duration pollDuration) {
        // assign topic
        TopicPartition partitionToReadFrom = new TopicPartition(this.topic, this.partition);
        this.kafkaConsumer.assign(List.of(partitionToReadFrom));
        // seek
        this.kafkaConsumer.seek(partitionToReadFrom, offSet);

        while (true) {
            ConsumerRecords<Object, Object> records = this.kafkaConsumer.poll(pollDuration);
            for (ConsumerRecord<Object, Object> record : records) {
                Message message = buildMessage(record);
                System.out.println(message.headers().toJson());
                System.out.println(message.body().toString());
            }
        }
    }

    public void run(Long offSet, Duration pollDuration) {
        try {
            if (!Thread.currentThread().isInterrupted()) {
                onRun(offSet, pollDuration);
            }
        } catch (KafkaException e) {
            LoggerUtil.logError("KafkaException caught on consumer thread", e);
        } catch (Exception e) {
            LoggerUtil.logError("Exception caught on consumer thread", e);
        } finally {
            onClose();
        }
    }

    private void onClose() {
        if (this.kafkaConsumer != null) {
            this.kafkaConsumer.unsubscribe();
            this.kafkaConsumer.close();
        }
    }
}
