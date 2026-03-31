package org.example.domain.service;

import org.example.domain.exception.DuplicateUserException;
import org.example.domain.model.CellType;
import org.example.domain.model.User;
import org.example.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для UserService.
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void register_ShouldCreateUser() {
        String login = "testuser";
        String password = "testpassword";
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.existsByLogin(login)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        doNothing().when(userRepository).save(any(User.class));

        User user = userService.register(login, password);

        assertNotNull(user);
        assertNotNull(user.id());
        assertEquals(login, user.login());
        assertEquals(encodedPassword, user.password());
        assertEquals(CellType.CROSS, user.symbol());

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    void register_ShouldThrowException_WhenLoginExists() {
        String login = "existinguser";
        String password = "testpassword";

        when(userRepository.existsByLogin(login)).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> userService.register(login, password));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByLogin_ShouldReturnUser_WhenExists() {
        String login = "testuser";
        User expectedUser = new User(UUID.randomUUID(), login, "password", CellType.CROSS);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findByLogin(login);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findByLogin_ShouldReturnEmpty_WhenNotExists() {
        String login = "nonexistentuser";

        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByLogin(login);

        assertFalse(result.isPresent());
    }

    @Test
    void validateCredentials_ShouldReturnTrue_WhenCredentialsValid() {
        String login = "testuser";
        String password = "testpassword";
        String encodedPassword = "$2a$10$encodedPassword";
        User user = new User(UUID.randomUUID(), login, encodedPassword, CellType.CROSS);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        boolean result = userService.validateCredentials(login, password);

        assertTrue(result);
    }

    @Test
    void validateCredentials_ShouldReturnFalse_WhenPasswordInvalid() {
        String login = "testuser";
        String password = "testpassword";
        String encodedPassword = "$2a$10$encodedPassword";
        User user = new User(UUID.randomUUID(), login, encodedPassword, CellType.CROSS);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        boolean result = userService.validateCredentials(login, "wrongpassword");

        assertFalse(result);
    }

    @Test
    void validateCredentials_ShouldReturnFalse_WhenUserNotExists() {
        String login = "nonexistentuser";
        String password = "testpassword";

        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

        boolean result = userService.validateCredentials(login, password);

        assertFalse(result);
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        UUID userId = UUID.randomUUID();
        User expectedUser = new User(userId, "testuser", "password", CellType.CROSS);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    void existsByLogin_ShouldReturnTrue_WhenExists() {
        String login = "testuser";

        when(userRepository.existsByLogin(login)).thenReturn(true);

        assertTrue(userService.existsByLogin(login));
    }

    @Test
    void existsByLogin_ShouldReturnFalse_WhenNotExists() {
        String login = "nonexistentuser";

        when(userRepository.existsByLogin(login)).thenReturn(false);

        assertFalse(userService.existsByLogin(login));
    }
}
