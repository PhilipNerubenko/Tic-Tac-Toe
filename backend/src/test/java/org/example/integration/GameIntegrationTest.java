package org.example.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для игрового процесса.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:gamedb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class GameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGame_ShouldCreateAndReturnSession() throws Exception {
        String userId = registerUser("gamecreator", "password123");
        String credentials = Base64.getEncoder().encodeToString("gamecreator:password123".getBytes());

        mockMvc.perform(post("/game")
                        .param("creatorId", userId)
                        .param("vsAi", "true")
                        .param("size", "3")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void playMove_ShouldProcessMoveAndReturnAiResponse() throws Exception {
        String userId = registerUser("player1", "password123");
        String credentials = Base64.getEncoder().encodeToString("player1:password123".getBytes());

        // Создаём игру
        String createResponse = mockMvc.perform(post("/game")
                        .param("creatorId", userId)
                        .param("vsAi", "true")
                        .param("size", "3")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("id").asText();

        // Делаем ход
        String moveJson = """
        {
          "gameMap": {
            "map": [[0,0,0],[0,1,0],[0,0,0]],
            "size": 3
          },
          "status": "PLAYER_TURN"
        }
        """;

        MvcResult result = mockMvc.perform(post("/game/" + gameId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveJson)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andReturn();

        // Проверяем, что ИИ сделал ответный ход
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        JsonNode gameMap = responseJson.get("gameMap").get("map");
        
        boolean hasZero = false;
        for (JsonNode row : gameMap) {
            for (JsonNode cell : row) {
                if (cell.asInt() == 2) {
                    hasZero = true;
                    break;
                }
            }
        }
        
        assert hasZero : "AI should have made a move (placed a zero)";
    }

    @Test
    void playMove_ShouldReturnBadRequest_WhenInvalidMove() throws Exception {
        String userId = registerUser("badplayer", "password123");
        String credentials = Base64.getEncoder().encodeToString("badplayer:password123".getBytes());

        String createResponse = mockMvc.perform(post("/game")
                        .param("creatorId", userId)
                        .param("vsAi", "true")
                        .param("size", "3")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("id").asText();

        String invalidMoveJson = """
        {
          "gameMap": {
            "map": [[1,1,0],[0,0,0],[0,0,0]],
            "size": 3
          },
          "status": "PLAYER_TURN"
        }
        """;

        mockMvc.perform(post("/game/" + gameId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMoveJson)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isBadRequest());
    }

    private String registerUser(String login, String password) throws Exception {
        String signUpJson = """
        {
          "login": "%s",
          "password": "%s"
        }
        """.formatted(login, password);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated());

        String credentials = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());
        String response = mockMvc.perform(post("/auth/signin")
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("userId").asText();
    }
}
