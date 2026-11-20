package ru.kata.spring.boot_security.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.util.UserNotCreatedException;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roles;
    public RoleServiceImpl(RoleRepository roles) { this.roles = roles; }
    @Transactional(readOnly = true) public Role findByName(String roleName) {
        return roles.findById(roleName).orElseThrow(() -> new UserNotCreatedException("Неизвестная роль: " + roleName));
    }
}
