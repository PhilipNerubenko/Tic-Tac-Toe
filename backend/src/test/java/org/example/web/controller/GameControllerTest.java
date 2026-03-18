package org.example.web.controller;

import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = GameController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserService userService;

    @Test
    void createGame_ShouldReturnCreatedStatus() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);

        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void playMove_ShouldReturnNextMove() throws Exception {
        UUID sessionId = UUID.randomUUID();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYING);

        when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(gameService.validateMapIntegrity(any(), any())).thenReturn(true);
        when(gameService.checkGameStatus(any())).thenReturn(GameStatus.PLAYING);

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,0,0],[0,0,0],[0,0,0]],
            "size": 3
          },
          "status": "PLAYING"
        }
        """;

        mockMvc.perform(post("/game/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYING"));
    }

    @Test
    void playMove_ShouldReturnBadRequest_WhenMapIsInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();

        GameSession existingSession = new GameSession(new GameMap(3));
        when(gameRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));

        when(gameService.validateMapIntegrity(any(), any())).thenReturn(false);

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,1,1],[1,1,1],[1,1,1]],
            "size": 3
          },
          "status": "PLAYING"
        }
        """;

        mockMvc.perform(post("/game/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isBadRequest());
    }

    @Test
    void playMove_ShouldSaveSession_WhenGameIsOver() throws Exception {
        UUID id = UUID.randomUUID();
        GameMap map = new GameMap();
        GameSession session = new GameSession(id, map, GameStatus.PLAYING);

        when(gameRepository.findById(id)).thenReturn(Optional.of(session));
        when(gameService.validateMapIntegrity(any(), any())).thenReturn(true);
        when(gameService.checkGameStatus(any())).thenReturn(GameStatus.CROSS_WIN);

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);

        String jsonPayload = """
                {
                    "gameMap": {"map": [[1,1,1],[0,0,0], [0,0,0]], "size": 3},
                    "status": "PLAYING"
                }
                """;

        mockMvc.perform(post("/game/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("Authorization", "Basic " + credentials));

        Mockito.verify(gameService, Mockito.never()).getNextMove(any());

        Mockito.verify(gameRepository, Mockito.times(1)).save(any(GameSession.class));
    }
}