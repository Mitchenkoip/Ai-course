package ru.kata.spring.boot_security.demo.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/** Describes a user account deletion for downstream systems. */
public record UserDeletedEvent(
        long userId,
        String email,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant deletedAt) {
}
