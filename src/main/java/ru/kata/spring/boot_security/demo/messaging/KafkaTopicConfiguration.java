package ru.kata.spring.boot_security.demo.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    /** Declares the single-partition topic that stores account deletion events. */
    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name(KafkaTopics.USER_DELETED)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
