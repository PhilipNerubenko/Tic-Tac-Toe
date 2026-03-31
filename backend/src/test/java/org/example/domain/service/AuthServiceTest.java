package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для AuthService.
 */
class AuthServiceTest {

    @Mock
    private UserService userService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userService);
    }

    @Test
    void signUp_ShouldReturnTrue_WhenRegistrationSuccessful() {
        RegistrationCommand command = new RegistrationCommand("testuser", "testpassword");

        when(userService.register(anyString(), anyString())).thenReturn(
                new User(UUID.randomUUID(), "testuser", "testpassword", CellType.CROSS)
        );

        boolean result = authService.signUp(command);

        assertTrue(result);
        verify(userService, times(1)).register("testuser", "testpassword");
    }

    @Test
    void signUp_ShouldThrowException_WhenUserAlreadyExists() {
        RegistrationCommand command = new RegistrationCommand("existinguser", "testpassword");

        when(userService.register(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("User with login 'existinguser' already exists"));

        assertThrows(IllegalArgumentException.class, () -> authService.signUp(command));

        verify(userService, times(1)).register("existinguser", "testpassword");
    }

    @Test
    void signIn_ShouldReturnUserId_WhenCredentialsValid() {
        String login = "testuser";
        String password = "testpassword";
        UUID expectedUserId = UUID.randomUUID();
        User user = new User(expectedUserId, login, password, CellType.CROSS);

        when(userService.validateCredentials(login, password)).thenReturn(true);
        when(userService.findByLogin(login)).thenReturn(Optional.of(user));

        UUID result = authService.signIn(login, password);

        assertEquals(expectedUserId, result);
        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, times(1)).findByLogin(login);
    }

    @Test
    void signIn_ShouldThrowException_WhenCredentialsInvalid() {
        String login = "testuser";
        String password = "wrongpassword";

        when(userService.validateCredentials(login, password)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(login, password));

        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, never()).findByLogin(anyString());
    }

    @Test
    void signIn_ShouldThrowException_WhenUserNotExists() {
        String login = "nonexistentuser";
        String password = "testpassword";

        when(userService.validateCredentials(login, password)).thenReturn(true);
        when(userService.findByLogin(login)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(login, password));

        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, times(1)).findByLogin(login);
    }
}
