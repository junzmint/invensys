package apigateway.component.kafkaproducer;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.joo.promise4j.Deferred;

public class KafkaProducer {
    private final String topic;
    private final Integer partition;
    private final org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer;

    public KafkaProducer(KafkaProducerConfig config) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(config.getKafkaProps());
    }

    private void ack(Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
        var msg = buildAckMessage(metadata);
        deferred.resolve(msg);
    }

    private Message buildAckMessage(RecordMetadata metadata) {
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

    public void send(Message message, Deferred<Message, Exception> deferred, String key) {
        var record = buildProducerRecord(this.topic, this.partition, key, message);
        if (deferred == null) {
            this.kafkaProducer.send(record, null);
        } else {
            this.kafkaProducer.send(record, (metadata, ex) -> ack(deferred, metadata, ex));
        }
    }

    public void onClose() {
        if (this.kafkaProducer != null)
            this.kafkaProducer.close();
    }
}
