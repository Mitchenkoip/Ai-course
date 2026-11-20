package ru.kata.spring.boot_security.demo.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Objects;
import ru.kata.spring.boot_security.demo.util.UserErrorResponse;
import ru.kata.spring.boot_security.demo.util.UserNotCreatedException;
import ru.kata.spring.boot_security.demo.util.UserNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserErrorResponse> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UserErrorResponse("User with this id wasn't found", System.currentTimeMillis()));
    }

    @ExceptionHandler(UserNotCreatedException.class)
    public ResponseEntity<UserErrorResponse> handleUserNotCreated(UserNotCreatedException exception) {
        return ResponseEntity.badRequest()
                .body(new UserErrorResponse(exception.getMessage(), System.currentTimeMillis()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<UserErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        Objects.requireNonNull(exception, "exception");
        if (!isEmailUniqueConstraintViolation(exception)) {
            throw exception;
        }

        return ResponseEntity.badRequest().body(new UserErrorResponse(
                "email - Человек с такой почтой уже существует",
                System.currentTimeMillis()));
    }

    private boolean isEmailUniqueConstraintViolation(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "uk_users_email".equals(violation.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
