package ru.kata.spring.boot_security.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.dto.RoleDTO;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.events.UserDeletedEvent;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.util.EmailUniquenessValidator;
import ru.kata.spring.boot_security.demo.util.UserNotCreatedException;
import ru.kata.spring.boot_security.demo.util.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final String ROLE_USER = "ROLE_USER";

    @Mock private UserRepository userRepository;
    @Mock private RoleService roleService;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailUniquenessValidator emailValidator;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void findAllMapsUsersAndKeepsStoredPassword() {
        User user = user(7L, "stored-hash");
        UserDTO mappedDto = validUserDto("stored-hash");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(mappedDto);

        List<UserDTO> result = userService.findAll();

        assertEquals(1, result.size());
        assertSame(mappedDto, result.getFirst());
        assertEquals("stored-hash", result.getFirst().getPassword());
    }

    @Test
    void findByIdReturnsMappedUser() {
        User user = user(7L, "stored-hash");
        UserDTO mappedDto = validUserDto(null);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(mappedDto);

        UserDTO result = userService.findById(7L);

        assertSame(mappedDto, result);
        assertEquals("stored-hash", result.getPassword());
    }

    @Test
    void findByIdThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(404L));
        verifyNoInteractions(modelMapper);
    }

    @Test
    void findByEmailReturnsRepositoryResult() {
        User user = user(7L, "stored-hash");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertSame(user, userService.findByEmail("user@example.com"));
    }

    @Test
    void findByEmailThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("missing@example.com"));
    }

    @Test
    void createEncodesPasswordResolvesRolesAndPersistsUser() {
        UserDTO dto = validUserDto("plain-password");
        Role role = new Role(ROLE_USER);
        when(emailValidator.emailAlreadyExists(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("bcrypt-hash");
        when(roleService.findByName(ROLE_USER)).thenReturn(role);

        userService.create(dto);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(savedUser.capture());
        assertEquals("bcrypt-hash", savedUser.getValue().getPassword());
        assertEquals(Set.of(role), savedUser.getValue().getRoles());
        assertEquals(dto.getEmail(), savedUser.getValue().getEmail());
    }

    @Test
    void createRejectsDuplicateEmailWithoutPersisting() {
        UserDTO dto = validUserDto("plain-password");
        when(emailValidator.emailAlreadyExists(dto.getEmail())).thenReturn(true);

        assertThrows(UserNotCreatedException.class, () -> userService.create(dto));

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(passwordEncoder, roleService);
    }

    @Test
    void createRejectsMissingPassword() {
        UserDTO dto = validUserDto(" ");

        assertThrows(UserNotCreatedException.class, () -> userService.create(dto));

        verifyNoInteractions(userRepository, passwordEncoder, roleService, emailValidator);
    }

    @Test
    void updateEncodesNewPasswordAndPersistsChanges() {
        User user = user(7L, "old-hash");
        UserDTO dto = validUserDto("new-password");
        dto.setLastName("Updated");
        dto.setAge(41);
        Role role = new Role(ROLE_USER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(roleService.findByName(ROLE_USER)).thenReturn(role);

        userService.update(7L, dto);

        assertEquals("Updated", user.getLastName());
        assertEquals(41, user.getAge());
        assertEquals("new-hash", user.getPassword());
        assertEquals(Set.of(role), user.getRoles());
        verify(userRepository).save(user);
    }

    @Test
    void updateDoesNotReencodeAnUnchangedPasswordHash() {
        User user = user(7L, "existing-hash");
        UserDTO dto = validUserDto("existing-hash");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(roleService.findByName(ROLE_USER)).thenReturn(new Role(ROLE_USER));

        userService.update(7L, dto);

        assertEquals("existing-hash", user.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(user);
    }

    @Test
    void updateKeepsCurrentPasswordWhenPasswordIsBlank() {
        User user = user(7L, "existing-hash");
        UserDTO dto = validUserDto("");
        dto.setAge(26);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(roleService.findByName(ROLE_USER)).thenReturn(new Role(ROLE_USER));

        userService.update(7L, dto);

        assertEquals(26, user.getAge());
        assertEquals("existing-hash", user.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(user);
    }

    @Test
    void updateThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        UserDTO updateRequest = validUserDto("plain-password");

        assertThrows(UserNotFoundException.class,
                () -> userService.update(404L, updateRequest));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, roleService);
    }

    @Test
    void deleteRemovesAnotherUser() {
        User user = user(7L, "stored-hash");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        userService.delete(7L, "admin@example.com");

        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        var deletionSequence = inOrder(userRepository, eventPublisher);
        deletionSequence.verify(userRepository).delete(user);
        deletionSequence.verify(eventPublisher).publishEvent(event.capture());
        UserDeletedEvent deletedEvent = (UserDeletedEvent) event.getValue();
        assertEquals(7L, deletedEvent.userId());
        assertEquals("user@example.com", deletedEvent.email());
    }

    @Test
    void deleteRejectsAuthenticatedUser() {
        User user = user(7L, "stored-hash");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThrows(UserNotCreatedException.class,
                () -> userService.delete(7L, "USER@example.com"));

        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deleteDoesNotPublishEventWhenUserDoesNotExist() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.delete(404L, "admin@example.com"));

        verifyNoInteractions(eventPublisher);
    }

    private static User user(long id, String password) {
        User user = new User("First", "Last", 30, "user@example.com", password,
                Set.of(new Role(ROLE_USER)));
        user.setId(id);
        return user;
    }

    private static UserDTO validUserDto(String password) {
        UserDTO dto = new UserDTO();
        dto.setFirstName("First");
        dto.setLastName("Last");
        dto.setAge(30);
        dto.setEmail("user@example.com");
        dto.setPassword(password);
        dto.setRoles(Set.of(new RoleDTO(ROLE_USER)));
        return dto;
    }
}
