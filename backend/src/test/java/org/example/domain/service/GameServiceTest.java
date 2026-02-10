package org.example.domain.service;

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
        GameSession session = new GameSession(map);

        gameService.getNextMove(session);

        assertThat(session.getGameMap().getMap()[0][2]).isEqualTo(2);
        assertThat(session.getStatus()).isEqualTo(GameStatus.ZERO_WIN);
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
        GameSession session = new GameSession(map);

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
        GameSession session = new GameSession(map);

        int[] result = gameService.getNextMove(session);

        assertArrayEquals(new int[]{2, 0}, result);

        assertThat(session.getGameMap().getMap()[2][0]).isEqualTo(2);

        assertThat(session.getStatus()).isEqualTo(GameStatus.DRAW);

        Mockito.verify(gameRepository, Mockito.times(1)).save(session);
    }

    @Test
    void getNextMove_ShouldUseEvaluateBoard_WhenManyMovesLeft() {
        int[][] emptyBoard = new int[3][3];
        GameMap map = new GameMap(emptyBoard, 3);
        GameSession session = new GameSession(map);

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
        GameMap oldMap = new GameMap(new int[][]{{0,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYING);

        GameMap newMap = new GameMap(new int[][]{{1,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenSessionDoesNotExist() {
        GameSession unknownSession = new GameSession(new GameMap(2));

        Mockito.when(gameRepository.findById(any())).thenReturn(Optional.empty());

        boolean isValid = gameService.validateMapIntegrity(unknownSession, unknownSession.getGameMap());

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenTwoCrossesAddedAtOnce() {
        UUID sessionId = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{0,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYING);

        GameMap newMap = new GameMap(new int[][]{{1,1},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnFalse_WhenExistingSignIsChanged() {
        UUID sessionId = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYING);

        GameMap newMap = new GameMap(new int[][]{{1,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateMapIntegrity_ShouldReturnTrue_WhenOldAndNewMapEquals() {
        UUID sessionId = UUID.randomUUID();
        GameMap oldMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);
        GameSession savedSession = new GameSession(sessionId, oldMap, GameStatus.PLAYING);

        GameMap newMap = new GameMap(new int[][]{{2,0},{0,0}}, 2);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(savedSession));

        boolean isValid = gameService.validateMapIntegrity(savedSession, newMap);

        assertThat(isValid).isFalse();
    }
}