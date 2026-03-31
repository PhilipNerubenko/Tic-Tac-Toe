package org.example.web.controller;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.model.User;
import org.example.domain.repository.GameRepository;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(gameService.validateMapIntegrity(any(), any())).thenReturn(true);
        when(gameService.checkGameStatus(any())).thenReturn(GameStatus.PLAYER_TURN);
        when(gameService.executeTurn(eq(sessionId), any(GameSession.class), eq(playerX))).thenAnswer(invocation -> {
            GameSession us = invocation.getArgument(1);
            // Копируем карту из us в session
            GameMap newMap = us.getGameMap();
            for (int i = 0; i < newMap.getSize(); i++) {
                for (int j = 0; j < newMap.getSize(); j++) {
                    session.getGameMap().setCellValue(i, j, newMap.getMap()[i][j] == 0 ? CellType.EMPTY : (newMap.getMap()[i][j] == 1 ? CellType.CROSS : CellType.ZERO));
                }
            }
            // После хода игрока, если игра не завершена, ход переключается на O (playerO)
            session.switchTurn();
            return session;
        });

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);
        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS);
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,0,0],[0,0,0],[0,0,0]],
            "size": 3
          },
          "status": "PLAYER_TURN"
        }
        """;

        mockMvc.perform(post("/game/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"));
    }

    @Test
    void playMove_ShouldReturnBadRequest_WhenMapIsInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();

        GameMap map = new GameMap(3);
        GameSession existingSession = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);
        when(gameRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));

        when(gameService.validateMapIntegrity(any(), any())).thenReturn(false);

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);
        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS);
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,1,1],[1,1,1],[1,1,1]],
            "size": 3
          },
          "status": "PLAYER_TURN"
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
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap();
        GameSession session = new GameSession(id, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        when(gameRepository.findById(id)).thenReturn(Optional.of(session));
        when(gameService.validateMapIntegrity(any(), any())).thenReturn(true);
        when(gameService.checkGameStatus(any())).thenReturn(GameStatus.VICTORY);
        when(gameService.executeTurn(eq(id), any(GameSession.class), eq(playerX))).thenAnswer(invocation -> {
            GameSession us = invocation.getArgument(1);
            // Обновляем карту
            GameMap newMap = us.getGameMap();
            for (int i = 0; i < newMap.getSize(); i++) {
                for (int j = 0; j < newMap.getSize(); j++) {
                    session.getGameMap().setCellValue(i, j, newMap.getMap()[i][j] == 0 ? CellType.EMPTY : (newMap.getMap()[i][j] == 1 ? CellType.CROSS : CellType.ZERO));
                }
            }
            // Устанавливаем статус победы
            session.setStatus(GameStatus.VICTORY);
            session.setWinner(playerX);
            session.setCurrentPlayer(null);
            return session;
        });

        String credentials = Base64.getEncoder().encodeToString("testuser:testpassword".getBytes());
        when(userService.validateCredentials("testuser", "testpassword")).thenReturn(true);
        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS);
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
                {
                    "gameMap": {"map": [[1,1,1],[0,0,0], [0,0,0]], "size": 3},
                    "status": "PLAYER_TURN"
                }
                """;

        mockMvc.perform(post("/game/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("Authorization", "Basic " + credentials));

        Mockito.verify(gameService, Mockito.never()).getNextMove(any());

        Mockito.verify(gameRepository, Mockito.times(1)).save(any(GameSession.class));
    }

    @Test
    void playMove_ShouldReturnForbidden_WhenNotYourTurn() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID(); // user1
        UUID playerO = UUID.randomUUID(); // user2

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);
        when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // user2 аутентифицируется, но в сессии currentPlayer = playerX (не его очередь)
        String credentials = Base64.getEncoder().encodeToString("user2:pass2".getBytes());
        when(userService.validateCredentials("user2", "pass2")).thenReturn(true);
        User user = new User(playerO, "user2", "pass2", CellType.ZERO);
        when(userService.findByLogin("user2")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[2,0,0],[0,0,0],[0,0,0]],
            "size": 3
          },
          "status": "PLAYER_TURN"
        }
        """;

        mockMvc.perform(post("/game/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload)
                        .header("Authorization", "Basic " + credentials))
                .andExpect(status().isForbidden());
    }
}