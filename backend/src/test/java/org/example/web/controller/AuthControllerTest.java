package org.example.web.controller;

import org.example.domain.model.User;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.example.web.model.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для AuthController.
 */
@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    
    @MockBean
    private UserService userService;

    @Test
    void signUp_ShouldReturnSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest("testuser", "testpassword");

        when(authService.signUp(any(SignUpRequest.class))).thenReturn(true);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"testuser\",\"password\":\"testpassword\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signIn_ShouldReturnUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());

        when(authService.signIn("testuser", "testpassword")).thenReturn(userId);

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void signIn_ShouldReturnUnauthorized_WhenCredentialsInvalid() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("testuser:wrongpassword".getBytes());

        when(authService.signIn("testuser", "wrongpassword"))
                .thenThrow(new IllegalArgumentException("Invalid login or password"));

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signIn_ShouldReturnBadRequest_WhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(post("/auth/signin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUserInfo() throws Exception {
        UUID userId = UUID.randomUUID();
        User testUser = new User(userId, "testuser", "testpassword", null);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/auth/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
