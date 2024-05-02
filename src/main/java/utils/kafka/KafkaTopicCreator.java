package utils.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class KafkaTopicCreator {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicCreator.class);

    public static void main(String[] args) throws IOException {
        KafkaTopicCreator.create(1, (short) 1);
    }

    public static void create(int partitions, short replications) throws IOException {
        // App config
        Properties appProps = new Properties();
        appProps.load(new FileInputStream("config/application.properties"));
        String bootstrapServer = appProps.getProperty("KAFKA_BROKER");
        String topicName = appProps.getProperty("KAFKA_TOPIC");

        // AdminClient config
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        // Create Kafka AdminClient
        try (AdminClient adminClient = AdminClient.create(props)) {
            int numPartitions = partitions;
            short replicationFactor = replications;

            // Check whether if topic is existed
            if (adminClient.listTopics().names().get().contains(topicName)) {
                // If existed, then delete
                adminClient.deleteTopics(Collections.singletonList(topicName)).all().get();
                logger.info("Topic '{}' deleted successfully", topicName);
                // Wait 5 sec to delete
                TimeUnit.SECONDS.sleep(5);
            }
            // Create new topic
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            logger.info("Topic '{}' created successfully with {} partition(s) and replication factor {}", topicName, numPartitions, replicationFactor);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating Kafka topic: {}", e.getMessage());
        }
    }
}

