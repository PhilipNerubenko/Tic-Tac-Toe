package org.example.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для авторизации.
 * Тесты проверяют полный цикл: HTTP запрос → Controller → Service → Repository → H2 БД.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signUpAndSignIn_ShouldWork() throws Exception {
        // Регистрация
        String signUpJson = """
        {
          "login": "integrationuser",
          "password": "integrationpass"
        }
        """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Авторизация
        String credentials = Base64.getEncoder().encodeToString("integrationuser:integrationpass".getBytes());

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void signIn_ShouldReturnUnauthorized_WhenWrongPassword() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("nonexistent:wrong".getBytes());

        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signUp_ShouldReturnConflict_WhenUserAlreadyExists() throws Exception {
        String signUpJson = """
        {
          "login": "duplicateuser",
          "password": "password123"
        }
        """;

        // Первая регистрация
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated());

        // Повторная регистрация с тем же логином — 409 Conflict
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Login already in use"));
    }
}
