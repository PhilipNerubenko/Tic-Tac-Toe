package org.example.web.controller;

import org.example.domain.model.CellType;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.domain.model.UserRole;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.example.web.model.JwtRequest;
import org.example.web.model.JwtResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    void signUp_ShouldReturnCreated() throws Exception {
        when(authService.signUp(any(RegistrationCommand.class))).thenReturn(true);

        String jsonPayload = """
        {
          "login": "newuser",
          "password": "newpassword"
        }
        """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signIn_ShouldReturnJwtTokens() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes());

        when(authService.signIn(any(JwtRequest.class))).thenReturn(new JwtResponse("Bearer", "access-token", "refresh-token"));

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void signIn_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes());

        when(authService.signIn(any(JwtRequest.class))).thenThrow(new IllegalArgumentException("Invalid login or password"));

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/" + userId))
                .andExpect(status().isNotFound());
    }
}
