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
 * Интеграционные тесты для SecurityConfig и AuthFilter.
 * Проверяют, что Spring Security корректно настраивает доступ к endpoint'ам.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitydb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signup_ShouldBeAccessibleWithoutAuth() throws Exception {
        String signUpJson = """
        {
          "login": "securitytest",
          "password": "securepass"
        }
        """;

        // /auth/signup должен быть доступен без авторизации
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated());
    }

    @Test
    void signin_ShouldBeAccessibleWithoutAuth() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("nonexistent:pass".getBytes());

        // /auth/signin должен быть доступен без авторизации
        mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swagger_ShouldBeAccessibleWithoutAuth() throws Exception {
        // Swagger UI должен быть доступен без авторизации
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound());
    }

    @Test
    void gameEndpoint_ShouldRequireAuth() throws Exception {
        // /game требует авторизацию — Spring Security вернёт 403 без аутентификации
        mockMvc.perform(post("/game")
                        .param("creatorId", "00000000-0000-0000-0000-000000000000")
                        .param("vsAi", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void gameEndpoint_ShouldWorkWithValidAuth() throws Exception {
        // Регистрируем пользователя
        String signUpJson = """
        {
          "login": "securitygameuser",
          "password": "gamepass"
        }
        """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated());

        // Получаем userId
        String credentials = Base64.getEncoder().encodeToString("securitygameuser:gamepass".getBytes());
        String response = mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        String userId = response.split("\"userId\":\"")[1].split("\"")[0];

        // Создаём игру с авторизацией
        mockMvc.perform(post("/game")
                        .param("creatorId", userId)
                        .param("vsAi", "true")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isCreated());
    }
}
