package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDTOValidationTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidUserData() {
        assertTrue(validator.validate(validUser()).isEmpty());
    }

    @Test
    void rejectsMissingAndOutOfRangeUserData() {
        UserDTO dto = validUser();
        dto.setFirstName("");
        dto.setLastName("A");
        dto.setAge(17);
        dto.setEmail("");
        dto.setPassword("");
        dto.setRoles(Set.of());

        Set<String> invalidFields = validator.validate(dto).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("firstName", "lastName", "age", "email", "roles"), invalidFields);
    }

    @Test
    void rejectsAgeAboveMaximum() {
        UserDTO dto = validUser();
        dto.setAge(151);

        assertTrue(validator.validate(dto).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("age")));
    }

    private static UserDTO validUser() {
        UserDTO dto = new UserDTO();
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        dto.setAge(36);
        dto.setEmail("ada@example.com");
        dto.setPassword("secret");
        dto.setRoles(Set.of(new RoleDTO("ROLE_USER")));
        return dto;
    }
}
