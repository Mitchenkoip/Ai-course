package ru.kata.spring.boot_security.demo.service;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dto.RoleDTO;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.events.UserDeletedEvent;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.util.EmailUniquenessValidator;
import ru.kata.spring.boot_security.demo.util.UserNotCreatedException;
import ru.kata.spring.boot_security.demo.util.UserNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailUniquenessValidator emailValidator;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleService roleService,
            ModelMapper modelMapper,
            PasswordEncoder passwordEncoder,
            EmailUniquenessValidator emailValidator,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailValidator = emailValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void create(UserDTO userDto) {
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new UserNotCreatedException("password - Password should not be empty");
        }
        if (emailValidator.emailAlreadyExists(userDto.getEmail())) {
            throw new UserNotCreatedException("email - Человек с такой почтой уже существует");
        }

        User user = new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getAge(),
                userDto.getEmail(),
                passwordEncoder.encode(userDto.getPassword()),
                resolveRoles(userDto.getRoles()));
        userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public void update(long id, UserDTO userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        copyUserFields(userDto, user);
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()
                && !userDto.getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        user.setRoles(resolveRoles(userDto.getRoles()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(long id, String authenticatedEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        if (user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new UserNotCreatedException("Нельзя удалить активную учётную запись. Войдите под другим администратором.");
        }
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeletedEvent(user.getId(), user.getEmail(), Instant.now()));
    }

    private void copyUserFields(UserDTO source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAge(source.getAge());
        target.setEmail(source.getEmail());
    }

    private Set<Role> resolveRoles(Set<RoleDTO> roleDtos) {
        return roleDtos.stream()
                .map(roleDto -> roleService.findByName(roleDto.getRoleName()))
                .collect(Collectors.toSet());
    }

    private UserDTO toDto(User user) {
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        userDto.setPassword(user.getPassword());
        return userDto;
    }
}
