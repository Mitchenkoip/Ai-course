package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;

/** Provides user lookup and account management operations. */
public interface UserService {
    /** Returns all users mapped to transfer objects. */
    List<UserDTO> findAll();

    /** Returns a user by ID, or throws {@code UserNotFoundException} when absent. */
    UserDTO findById(long id);

    /** Finds an account by email, or throws {@code UserNotFoundException} when absent. */
    User findByEmail(String email);

    /** Creates an account after validating its email and encoding its password. */
    void create(UserDTO dto);

    /** Updates an account; a blank password keeps the current password. */
    void update(long id, UserDTO dto);

    /** Deletes an account unless it belongs to the currently authenticated user. */
    void delete(long id, String authenticatedEmail);
}
