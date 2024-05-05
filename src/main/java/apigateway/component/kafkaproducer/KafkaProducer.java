package apigateway.component.kafkaproducer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import lombok.NonNull;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.joo.promise4j.Deferred;
import utils.logging.LoggerUtil;

public class KafkaProducer {
    private final String topic;
    private final Integer partition;
    private final org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer;

    public KafkaProducer(final @NonNull KafkaProducerConfig config) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(config.getKafkaProps());
    }

    // call back func for produce
    private void onProduce(Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            LoggerUtil.logInfo("Kafka producer send: " + buildOnSendMessage(metadata).headers().toString());
        } else {
            LoggerUtil.logError("Kafka producer error: ", exception);
            deferred.resolve(Message.ofAny(exception.getMessage()));
        }
    }

    private Message buildOnSendMessage(RecordMetadata metadata) {
        if (metadata == null)
            return null;
        var headers = BObject.ofEmpty().setAny(KafkaProducerConstants.IS_ACK_MSG, true)
                .setAny(KafkaProducerConstants.TIMESTAMP, metadata.timestamp())
                .setAny(KafkaProducerConstants.OFFSET, metadata.offset())
                .setAny(KafkaProducerConstants.PARTITION, metadata.partition())
                .setAny(KafkaProducerConstants.TOPIC, metadata.topic());
        return Message.ofAny(headers, BValue.ofEmpty());
    }

    private Object convert(BElement body) {
        if (body == null || body.isNullValue()) {
            return null;
        }
        if (body.isValue()) {
            return body.asValue().getData();
        }
        return body.toString();
    }

    // create record to produce
    private ProducerRecord<Object, Object> buildProducerRecord(String topic, Integer partition, String key, Message message) {
        var headers = message.headers();
        var body = message.body();

        var record = new ProducerRecord<Object, Object>(topic, partition, key, convert(body));

        if (body != null) {
            record.headers().add(KafkaProducerConstants.IS_VALUE, new byte[]{1});
        }
        for (var header : headers.entrySet()) {
            if (header.getValue().isValue()) {
                record.headers().add(header.getKey(), header.getValue().asValue().toBytes());
            }
        }
        return record;
    }

    public void produce(Message message, Deferred<Message, Exception> deferred, String key) {
        var record = buildProducerRecord(this.topic, this.partition, key, message);
        this.kafkaProducer.send(record, (metadata, ex) -> onProduce(deferred, metadata, ex));
    }

    public void onClose() {
        if (this.kafkaProducer != null)
            this.kafkaProducer.close();
    }
}
