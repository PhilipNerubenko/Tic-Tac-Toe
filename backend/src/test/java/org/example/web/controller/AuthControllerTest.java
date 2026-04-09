package org.example.web.controller;

import org.example.domain.model.CellType;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
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
    void signIn_ShouldReturnUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes());
        
        when(authService.signIn("user", "pass")).thenReturn(userId);

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void signIn_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes());
        
        when(authService.signIn("user", "wrong")).thenThrow(new IllegalArgumentException("Invalid login or password"));

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isUnauthorized());
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenRequestingOwnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "testpassword", CellType.CROSS);
        
        // Мокаем Authentication для SecurityContextHolder
        Authentication mockAuth = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("testuser");
        when(mockAuth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(mockAuth);
        
        // Мокаем поиск текущего пользователя и целевого пользователя
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"));
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenRequestingOtherUserProfile() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User requester = new User(requesterId, "requester", "password", CellType.CROSS);
        
        // Мокаем Authentication для SecurityContextHolder
        Authentication mockAuth = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("requester");
        when(mockAuth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(mockAuth);
        
        // Мокаем поиск текущего пользователя
        when(userService.findByLogin("requester")).thenReturn(Optional.of(requester));

        mockMvc.perform(get("/auth/" + targetId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "testpassword", CellType.CROSS);
        
        // Мокаем Authentication для SecurityContextHolder
        Authentication mockAuth = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("testuser");
        when(mockAuth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(mockAuth);
        
        // Мокаем поиск текущего пользователя
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));
        // Целевой пользователь не найден
        when(userService.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        
        // Не устанавливаем Authentication - SecurityContext пуст
        
        mockMvc.perform(get("/auth/" + userId))
                .andExpect(status().isForbidden());
    }
}
