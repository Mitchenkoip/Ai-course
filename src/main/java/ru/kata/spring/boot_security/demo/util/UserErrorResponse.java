package ru.kata.spring.boot_security.demo.util;

public class UserErrorResponse {
    private String message;
    private long timestamp;
    public UserErrorResponse(String message, long timestamp) { this.message = message; this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
