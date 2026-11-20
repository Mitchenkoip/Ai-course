package ru.kata.spring.boot_security.demo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailUniquenessValidatorTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private EmailUniquenessValidator validator;

    @Test
    void returnsTrueWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertTrue(validator.emailAlreadyExists("taken@example.com"));
        verify(userRepository).existsByEmail("taken@example.com");
    }

    @Test
    void returnsFalseWhenEmailIsAvailable() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertFalse(validator.emailAlreadyExists("new@example.com"));
        verify(userRepository).existsByEmail("new@example.com");
    }
}
