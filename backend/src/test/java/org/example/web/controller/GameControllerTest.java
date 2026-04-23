package org.example.web.controller;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.model.User;
import org.example.domain.model.UserRole;
import org.example.domain.repository.GameRepository;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
        // Очищаем SecurityContext перед каждым тестом
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createGame_ShouldReturnCreatedStatus() throws Exception {
        UUID creatorId = UUID.randomUUID();
        
        GameSession newSession = new GameSession(new GameMap(3), creatorId, true);
        when(gameService.createGame(3, creatorId, true)).thenReturn(newSession);

        mockMvc.perform(post("/game")
                        .param("creatorId", creatorId.toString())
                        .param("vsAi", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void playMove_ShouldReturnNextMove() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

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

        // Устанавливаем аутентификацию в SecurityContext
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,0,0],[0,0,0],[0,0,0]],
            "size": 3
          }
        }
        """;

        mockMvc.perform(post("/game/" + sessionId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"));
    }

    @Test
    void playMove_ShouldReturnBadRequest_WhenMapIsInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();

        when(gameService.executeTurn(eq(sessionId), any(GameSession.class), eq(playerX)))
                .thenThrow(new org.example.domain.exception.IntegrityViolationException());

        // Устанавливаем аутентификацию в SecurityContext
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[1,1,1],[1,1,1],[1,1,1]],
            "size": 3
          }
        }
        """;

        mockMvc.perform(post("/game/" + sessionId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void playMove_ShouldSaveSession_WhenGameIsOver() throws Exception {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap();
        GameSession session = new GameSession(id, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

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

        // Устанавливаем аутентификацию в SecurityContext
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        String jsonPayload = """
                {
                    "gameMap": {"map": [[1,1,1],[0,0,0], [0,0,0]], "size": 3}
                }
                """;

        mockMvc.perform(post("/game/" + id + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk());

        Mockito.verify(gameService, Mockito.never()).getNextMove(any());
    }

    @Test
    void playMove_ShouldReturnForbidden_WhenNotYourTurn() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID(); // user1
        UUID playerO = UUID.randomUUID(); // user2
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        when(gameService.executeTurn(eq(sessionId), any(GameSession.class), eq(playerO)))
                .thenThrow(new org.example.domain.exception.NotYourTurnException("Вы не можете ходить, сейчас очередь другого игрока"));

        // Устанавливаем аутентификацию в SecurityContext для user2
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user2", "pass2");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerO, "user2", "pass2", CellType.ZERO, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("user2")).thenReturn(Optional.of(user));

        String jsonPayload = """
        {
          "gameMap": {
            "map": [[2,0,0],[0,0,0],[0,0,0]],
            "size": 3
          }
        }
        """;

        mockMvc.perform(post("/game/" + sessionId + "/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGameById_ShouldReturnGame() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, GameSession.AI_PLAYER_ID, playerX, null, lastActiveAt, createdAt);

        when(gameService.findGameForUser(sessionId, playerX)).thenReturn(session);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/game/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()));
    }

    @Test
    void joinGame_ShouldJoinPlayer() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.WAITING_FOR_PLAYERS, creatorId, null, creatorId, null, lastActiveAt, createdAt);

        when(gameService.joinPlayer(sessionId, guestId)).thenReturn(session);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(guestId, "testuser", "testpassword", CellType.ZERO, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/game/" + sessionId + "/join")
                        .param("guestId", guestId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getActiveGames_ShouldReturnGames() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.WAITING_FOR_PLAYERS, creatorId, null, creatorId, null, lastActiveAt, createdAt);

        Map<UUID, GameSession> activeGames = Map.of(sessionId, session);
        when(gameService.getActiveGames()).thenReturn(activeGames);

        mockMvc.perform(get("/game/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + sessionId).exists());
    }

    @Test
    void checkOpponentLeft_ShouldReturnUpdatedGame() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, UUID.randomUUID(), playerX, null, lastActiveAt, createdAt);

        when(gameService.checkOpponentLeft(sessionId, playerX, 30)).thenReturn(session);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", "testpassword");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(playerX, "testuser", "testpassword", CellType.CROSS, Collections.singletonList(UserRole.USER));
        when(userService.findByLogin("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/game/" + sessionId + "/check-opponent-left")
                        .param("timeoutSeconds", "30"))
                .andExpect(status().isOk());
    }
}
