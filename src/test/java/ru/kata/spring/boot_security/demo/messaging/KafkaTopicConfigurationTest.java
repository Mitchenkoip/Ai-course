package ru.kata.spring.boot_security.demo.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopicConfigurationTest {
    @Test
    void declaresSinglePartitionTopicForUserDeletionEvents() {
        NewTopic topic = new KafkaTopicConfiguration().userDeletedTopic();

        assertEquals(KafkaTopics.USER_DELETED, topic.name());
        assertEquals(1, topic.numPartitions());
        assertEquals(1, topic.replicationFactor());
    }
}
