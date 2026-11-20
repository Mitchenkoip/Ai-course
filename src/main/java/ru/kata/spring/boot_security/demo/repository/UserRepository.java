package ru.kata.spring.boot_security.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "roles") Optional<User> findByEmail(String email);
    @Override @EntityGraph(attributePaths = "roles") List<User> findAll();
    @Override @EntityGraph(attributePaths = "roles") Optional<User> findById(Long id);
}
