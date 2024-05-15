package processor.component.kafka.topic_creator;

import logging.LoggerUtil;
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

            // Check whether if topic is existed
            if (adminClient.listTopics().names().get().contains(topicName)) {
                // If existed, then delete
                adminClient.deleteTopics(Collections.singletonList(topicName)).all().get();
                LoggerUtil.logInfo("TOPIC_DELETED: " + topicName);
                // Wait 5 sec to delete
                TimeUnit.SECONDS.sleep(5);
            }
            // Create new topic
            NewTopic newTopic = new NewTopic(topicName, partitions, replications);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            LoggerUtil.logInfo("TOPIC_CREATED: " + topicName);
        } catch (InterruptedException | ExecutionException e) {
            LoggerUtil.logError("KAFKA_TOPIC_CREATE_ERROR: ", e);
        }
    }
}

