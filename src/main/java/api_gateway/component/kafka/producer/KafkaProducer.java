package api_gateway.component.kafka.producer;

import api_gateway.component.ApiGatewayLogger;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import lombok.NonNull;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.joo.promise4j.Deferred;

public class KafkaProducer {
    private final String topic;
    private final Integer partition;
    private final org.apache.kafka.clients.producer.KafkaProducer<Object, Object> kafkaProducer;

    public KafkaProducer(final @NonNull KafkaProducerConfig config) {
        this.topic = config.getTopic();
        this.partition = config.getPartition();
        this.kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(config.getKafkaProps());
    }

    public void produce(Message message, Deferred<Message, Exception> deferred, String key, Boolean isAck) {
        var record = buildProducerRecord(this.topic, this.partition, key, message);
        if (record.value() == null) {
            deferred.resolve(Message.ofAny("BODY_NULL_VALUE"));
            return;
        }
        this.kafkaProducer.send(record, (metadata, ex) -> onProduce(isAck, deferred, metadata, ex));
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
            if (header.getValue().isValue())
                record.headers().add(header.getKey(), header.getValue().asValue().toBytes());
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
        return body.toJson();
    }

    // call back func for produce
    private void onProduce(Boolean isAck, Deferred<Message, Exception> deferred, RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            ApiGatewayLogger.logKafkaProducerInfo("KAFKA_PRODUCED", deferred);
            if (isAck) {
                ApiGatewayLogger.logKafkaProducerInfo("KAFKA_PRODUCED: ", buildAckMessage(metadata).headers().toString(), deferred);
                deferred.resolve(buildAckMessage(metadata));
            }
        } else {
            ApiGatewayLogger.logApiGatewayError("KAFKA_PRODUCE_ERROR", exception);
            deferred.resolve(Message.ofAny(exception.getMessage()));
        }
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

    public void onClose() {
        if (this.kafkaProducer != null)
            this.kafkaProducer.close();
    }
}
