package ru.kata.spring.boot_security.demo.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDeletedEventSerializationTest {
    @Test
    void serializesEventAsReadableJsonWithIsoTimestamp() throws Exception {
        UserDeletedEvent event = new UserDeletedEvent(
                17L,
                "deleted@example.com",
                Instant.parse("2026-11-18T12:00:00Z"));

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(event);

        assertEquals("{\"userId\":17,\"email\":\"deleted@example.com\","
                + "\"deletedAt\":\"2026-11-18T12:00:00Z\"}", json);
    }
}
