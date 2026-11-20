package ru.kata.spring.boot_security.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.kata.spring.boot_security.demo.events.UserDeletedEvent;

/** Sends committed account-deletion events to Kafka. */
@Component
public class KafkaUserDeletedEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUserDeletedEventListener.class);

    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    public KafkaUserDeletedEventListener(KafkaTemplate<String, UserDeletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(UserDeletedEvent event) {
        kafkaTemplate.send(KafkaTopics.USER_DELETED, Long.toString(event.userId()), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        LOGGER.error("Could not publish user deletion event for userId={}", event.userId(), failure);
                        return;
                    }
                    LOGGER.info("Published user deletion event for userId={} to topic {}", event.userId(),
                            result.getRecordMetadata().topic());
                });
    }
}
