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
    private final org.apache.kafka.clients.producer.KafkaProducer<Object, Object> producer;

    public KafkaProducer(KafkaProducerConfig config) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(config.getKafkaProps());
    }

    private void ack(Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
        var msg = buildAckMessage(metadata);
        deferred.resolve(msg);
    }

    private Message buildAckMessage(RecordMetadata metadata) {
        if (metadata == null)
            return null;
        var headers = BObject.ofEmpty().setAny("kafka.IS_ACK_MSG", "true")
                .setAny("kafka.TIMESTAMP", metadata.timestamp())
                .setAny("kafka.OFFSET", metadata.offset())
                .setAny("kafka.PARTITION", metadata.partition())
                .setAny("kafka.TOPIC", metadata.topic());
        return Message.ofAny(headers, BValue.ofEmpty());
    }

    private ProducerRecord<Object, Object> buildProducerRecord(String topic, Integer partition, Message message) {
        var headers = message.headers();

        var body = message.body();
        var record = new ProducerRecord<Object, Object>(topic, partition, "key", convert(body));
        if (body != null && body.isValue()) {
            record.headers().add("kafka.IS_VALUE", new byte[]{1});
        }

        for (var header : headers.entrySet()) {
            if (header.getValue().isValue()) {
                record.headers().add(header.getKey(), header.getValue().asValue().toBytes());
            }
        }

        return record;
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

    public void send(Message message, Deferred<Message, Exception> deferred) {

        var record = buildProducerRecord(this.topic, this.partition, message);
        if (deferred == null) {
            this.producer.send(record, null);
        } else {
            this.producer.send(record, (metadata, ex) -> ack(deferred, metadata, ex));
        }
    }

    protected void onStop() {
        if (this.producer != null)
            this.producer.close();
    }
}
