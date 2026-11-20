package ru.kata.spring.boot_security.demo.messaging;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kata.spring.boot_security.demo.events.UserDeletedEvent;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaUserDeletedEventListenerTest {
    @Mock
    private KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;
    @Mock
    private SendResult<String, UserDeletedEvent> sendResult;
    @Mock
    private RecordMetadata metadata;

    @Test
    void publishesUserDeletionToTopicUsingUserIdAsKey() {
        UserDeletedEvent event = new UserDeletedEvent(17L, "deleted@example.com", Instant.parse("2026-11-18T12:00:00Z"));
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.topic()).thenReturn(KafkaTopics.USER_DELETED);
        when(kafkaTemplate.send(eq(KafkaTopics.USER_DELETED), eq("17"), same(event)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        KafkaUserDeletedEventListener listener = new KafkaUserDeletedEventListener(kafkaTemplate);

        listener.publish(event);

        verify(kafkaTemplate).send(KafkaTopics.USER_DELETED, "17", event);
    }
}
