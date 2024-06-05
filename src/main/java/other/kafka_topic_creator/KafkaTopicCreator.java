package other.kafka_topic_creator;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import processor.component.ProcessorConstants;
import processor.component.kafka.consumer.KafkaConsumerLogger;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class KafkaTopicCreator {
    public static void main(String[] args) throws IOException {
        KafkaTopicCreator.create(1, (short) 1);
    }

    public static void create(int partitions, short replications) throws IOException {
        // App config
        String bootstrapServer = ProcessorConstants.getKafkaBroker();
        String topicName = ProcessorConstants.getKafkaTopic();

        // AdminClient config
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        // Create Kafka AdminClient
        try (AdminClient adminClient = AdminClient.create(props)) {

            // Check whether if topic is existed
            if (adminClient.listTopics().names().get().contains(topicName)) {
                // If existed, then delete
                adminClient.deleteTopics(Collections.singletonList(topicName)).all().get();
                System.out.println("TOPIC_DELETED: " + topicName);
                // Wait 5 sec to delete
                TimeUnit.SECONDS.sleep(5);
            }
            // Create new topic
            NewTopic newTopic = new NewTopic(topicName, partitions, replications);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            System.out.println("TOPIC_CREATED: " + topicName);
        } catch (InterruptedException | ExecutionException exception) {
            KafkaConsumerLogger.logProcessorError("KAFKA_TOPIC_CREATE_ERROR", exception);
        }
    }
}
