package ru.kata.spring.boot_security.demo.util;

import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

@Component
public class EmailUniquenessValidator {
    private final UserRepository userRepository;

    public EmailUniquenessValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean emailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
