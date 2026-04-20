package org.example.domain.service;

import org.example.domain.exception.DuplicateUserException;
import org.example.domain.model.CellType;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.domain.model.UserRole;
import org.example.web.model.JwtRequest;
import org.example.web.model.JwtResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
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

    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userService, jwtProvider);
    }

    @Test
    void signUp_ShouldReturnTrue_WhenRegistrationSuccessful() {
        RegistrationCommand command = new RegistrationCommand("testuser", "testpassword");

        when(userService.register(anyString(), anyString())).thenReturn(
                new User(UUID.randomUUID(), "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER))
        );

        boolean result = authService.signUp(command);

        assertTrue(result);
        verify(userService, times(1)).register("testuser", "testpassword");
    }

    @Test
    void signUp_ShouldThrowException_WhenUserAlreadyExists() {
        RegistrationCommand command = new RegistrationCommand("existinguser", "testpassword");

        when(userService.register(anyString(), anyString()))
                .thenThrow(new DuplicateUserException("Login already in use"));

        assertThrows(DuplicateUserException.class, () -> authService.signUp(command));

        verify(userService, times(1)).register("existinguser", "testpassword");
    }

    @Test
    void signIn_ShouldReturnJwtResponse_WhenCredentialsValid() {
        String login = "testuser";
        String password = "testpassword";
        UUID expectedUserId = UUID.randomUUID();
        User user = new User(expectedUserId, login, password, CellType.CROSS, Collections.singletonList(UserRole.USER));

        when(userService.validateCredentials(login, password)).thenReturn(true);
        when(userService.findByLogin(login)).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh-token");

        JwtResponse result = authService.signIn(new JwtRequest(login, password));

        assertEquals("Bearer", result.type());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, times(1)).findByLogin(login);
    }

    @Test
    void signIn_ShouldThrowException_WhenCredentialsInvalid() {
        String login = "testuser";
        String password = "wrongpassword";

        when(userService.validateCredentials(login, password)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(new JwtRequest(login, password)));

        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, never()).findByLogin(anyString());
    }

    @Test
    void signIn_ShouldThrowException_WhenUserNotExists() {
        String login = "nonexistentuser";
        String password = "testpassword";

        when(userService.validateCredentials(login, password)).thenReturn(true);
        when(userService.findByLogin(login)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(new JwtRequest(login, password)));

        verify(userService, times(1)).validateCredentials(login, password);
        verify(userService, times(1)).findByLogin(login);
    }
}
