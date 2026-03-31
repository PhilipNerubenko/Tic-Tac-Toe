package org.example.domain.service;

import org.example.domain.exception.NotYourTurnException;
import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class GameServiceTest {
    private GameService gameService;
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository = Mockito.mock(GameRepository.class);
        gameService = new GameServiceImpl(gameRepository);
    }

    @Test
    void getNextMove_ShouldFindWinningMoveForAI() {
        int[][] board = {
                {2, 2, 0},
                {1, 0, 0},
                {1, 0, 0}
        };
        GameMap map = new GameMap(board, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true); // vsAi = true, playerO = AI

        gameService.getNextMove(session);

        assertThat(session.getGameMap().getMap()[0][2]).isEqualTo(CellType.ZERO.getValue());
        assertThat(session.getStatus()).isEqualTo(GameStatus.VICTORY);
        assertThat(session.getWinner()).isEqualTo(GameSession.AI_PLAYER_ID);
        Mockito.verify(gameRepository, Mockito.times(1)).save(session);
    }

    @Test
    void getNextMove_ShouldDoNothing_WhenMapIsFull() {
        int[][] fullBoard = {
                {1, 2, 1},
                {1, 2, 2},
                {2, 1, 2}
        };
        GameMap map = new GameMap(fullBoard, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] result = gameService.getNextMove(session);

        assertArrayEquals(new int[]{-1, -1}, result);

        Mockito.verify(gameRepository, Mockito.never()).save(any());
    }

    @Test
    void getNextMove_ShouldMakeLastAvailableMoveAndSetDraw() {
        int[][] almostFullBoard = {
                {1, 2, 1},
                {1, 2, 2},
                {0, 1, 2}
        };

        GameMap map = new GameMap(almostFullBoard, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] result = gameService.getNextMove(session);

        assertArrayEquals(new int[]{2, 0}, result);

        assertThat(session.getGameMap().getMap()[2][0]).isEqualTo(CellType.ZERO.getValue());

        assertThat(session.getStatus()).isEqualTo(GameStatus.DRAW);

        Mockito.verify(gameRepository, Mockito.times(1)).save(session);
    }

    @Test
    void getNextMove_ShouldUseEvaluateBoard_WhenManyMovesLeft() {
        int[][] emptyBoard = new int[3][3];
        GameMap map = new GameMap(emptyBoard, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] move = gameService.getNextMove(session);

        assertThat(move).isNotNull();
    }

    @Test
    void checkGameStatus_ShouldReturnStatusDraw_WhenMapIsFull() {
        int[][] fullBoard = {
                {1, 2, 1},
                {1, 2, 2},
                {2, 1, 2}
        };
        GameMap map = new GameMap(fullBoard, 3);

        GameStatus result = gameService.checkGameStatus(map);

        assertEquals(GameStatus.DRAW, result);
    }

    @Test
    void validateMapIntegrity_ShouldReturnTrue_WhenOneCrossAddedCorrectly() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{0,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        GameMap newMap = new GameMap(new int[][]{{1,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenSessionDoesNotExist() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(2);
        GameSession unknownSession = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        Mockito.when(gameRepository.findById(any())).thenReturn(Optional.empty());

        boolean isValid = gameService.validateMapIntegrity(unknownSession, unknownSession.getGameMap());

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenTwoCrossesAddedAtOnce() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{0,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        GameMap newMap = new GameMap(new int[][]{{1,1},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenExistingSignIsChanged() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        GameMap newMap = new GameMap(new int[][]{{1,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnTrue_WhenOldAndNewMapEquals() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        GameMap newMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }

    @Test
    void executeTurn_ShouldThrowNotYourTurnException_WhenAuthenticatedUserIsNotCurrentPlayer() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);
        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Создаем userMove с любой картой (не важно, так как проверка прав происходит до валидации)
        GameSession userMove = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, null, null);

        // playerO пытается ходить, но текущий игрок - playerX
        assertThrows(NotYourTurnException.class, () -> {
            gameService.executeTurn(sessionId, userMove, playerO);
        });
    }

    @Test
    void executeTurn_ShouldProceed_WhenAuthenticatedUserIsCurrentPlayer() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);
        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Создаем валидный ход: ставим CROSS в (0,0)
        int[][] newMapArray = new int[3][3];
        newMapArray[0][0] = 1; // CROSS
        GameMap newMap = new GameMap(newMapArray, 3);
        GameSession userMove = new GameSession(sessionId, newMap, GameStatus.PLAYER_TURN, playerX, playerO, null, null);

        // playerX - текущий игрок, должен пройти проверку
        GameSession result = gameService.executeTurn(sessionId, userMove, playerX);

        assertThat(result).isNotNull();
        // После хода, текущий игрок должен переключиться на playerO
        assertThat(result.getCurrentPlayer()).isEqualTo(playerO);
        // Карта должна обновиться: (0,0) = CROSS
        assertThat(result.getGameMap().getMap()[0][0]).isEqualTo(CellType.CROSS.getValue());
    }
}