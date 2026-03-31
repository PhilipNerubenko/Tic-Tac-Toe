package org.example.domain.service;

import org.example.domain.exception.GameDomainException;
import org.example.domain.exception.GameNotFoundException;
import org.example.domain.exception.IntegrityViolationException;
import org.example.domain.exception.NotYourTurnException;
import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
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

    @Test
    void createGame_ShouldCreateAndSaveSession() {
        int size = 3;
        UUID creatorId = UUID.randomUUID();

        GameSession result = gameService.createGame(size, creatorId, true);

        assertThat(result).isNotNull();
        assertThat(result.getPlayerX()).isEqualTo(creatorId);
        assertThat(result.getPlayerO()).isEqualTo(GameSession.AI_PLAYER_ID);
        assertThat(result.getGameMap().getSize()).isEqualTo(size);
        Mockito.verify(gameRepository, Mockito.times(1)).save(result);
    }

    @Test
    void createGame_ShouldCreateMultiplayerGame_WhenVsAiFalse() {
        int size = 3;
        UUID creatorId = UUID.randomUUID();

        GameSession result = gameService.createGame(size, creatorId, false);

        assertThat(result).isNotNull();
        assertThat(result.getPlayerX()).isEqualTo(creatorId);
        assertThat(result.getPlayerO()).isNull();
        assertThat(result.getStatus()).isEqualTo(GameStatus.WAITING_FOR_PLAYERS);
    }

    @Test
    void joinPlayer_ShouldJoinOpponent() {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.WAITING_FOR_PLAYERS, creatorId, null, creatorId, null);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        GameSession result = gameService.joinPlayer(sessionId, guestId);

        assertThat(result.getPlayerO()).isEqualTo(guestId);
        assertThat(result.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
        assertThat(result.getCurrentPlayer()).isEqualTo(creatorId);
        Mockito.verify(gameRepository, Mockito.times(1)).save(session);
    }

    @Test
    void joinPlayer_ShouldThrowException_WhenAiGame() {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(map, creatorId, true);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(GameDomainException.class, () -> gameService.joinPlayer(sessionId, guestId));
    }

    @Test
    void joinPlayer_ShouldThrowException_WhenGameFull() {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.WAITING_FOR_PLAYERS, creatorId, playerO, creatorId, null);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(GameDomainException.class, () -> gameService.joinPlayer(sessionId, guestId));
    }

    @Test
    void joinPlayer_ShouldThrowException_WhenGameStarted() {
        UUID sessionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, creatorId, null, creatorId, null);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(GameDomainException.class, () -> gameService.joinPlayer(sessionId, guestId));
    }

    @Test
    void joinPlayer_ShouldThrowException_WhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.joinPlayer(sessionId, guestId));
    }

    @Test
    void getActiveGames_ShouldReturnOnlyWaitingMultiplayerGames() {
        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();
        UUID sessionId3 = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        // Waiting multiplayer game
        GameMap map1 = new GameMap(3);
        GameSession waitingGame = new GameSession(sessionId1, map1, GameStatus.WAITING_FOR_PLAYERS, creatorId, null, creatorId, null);

        // AI game (should be excluded)
        GameSession aiGame = new GameSession(map1, creatorId, true);

        // Full game (should be excluded)
        UUID playerO = UUID.randomUUID();
        GameSession fullGame = new GameSession(sessionId3, map1, GameStatus.PLAYER_TURN, creatorId, playerO, creatorId, null);

        Map<UUID, GameSession> allGames = new HashMap<>();
        allGames.put(sessionId1, waitingGame);
        allGames.put(sessionId2, aiGame);
        allGames.put(sessionId3, fullGame);

        Mockito.when(gameRepository.findAll()).thenReturn(allGames);

        Map<UUID, GameSession> result = gameService.getActiveGames();

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(sessionId1);
    }

    @Test
    void executeTurn_ShouldThrowGameNotFoundException_WhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession userMove = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, UUID.randomUUID(), null, null, null);

        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.executeTurn(sessionId, userMove, UUID.randomUUID()));
    }

    @Test
    void executeTurn_ShouldThrowIntegrityViolationException_WhenMapInvalid() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);
        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Невалидный ход: добавлено 2 крестика
        int[][] newMapArray = new int[3][3];
        newMapArray[0][0] = 1;
        newMapArray[0][1] = 1;
        GameMap newMap = new GameMap(newMapArray, 3);
        GameSession userMove = new GameSession(sessionId, newMap, GameStatus.PLAYER_TURN, playerX, playerO, null, null);

        assertThrows(IntegrityViolationException.class, () -> gameService.executeTurn(sessionId, userMove, playerX));
    }

    @Test
    void executeTurn_ShouldMakeAiMove_WhenVsAiGame() {
        UUID sessionId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        GameMap map = new GameMap(3);
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, GameSession.AI_PLAYER_ID, playerX, null);
        Mockito.when(gameRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Валидный ход: ставим CROSS в (0,0)
        int[][] newMapArray = new int[3][3];
        newMapArray[0][0] = 1; // CROSS
        GameMap newMap = new GameMap(newMapArray, 3);
        GameSession userMove = new GameSession(sessionId, newMap, GameStatus.PLAYER_TURN, playerX, GameSession.AI_PLAYER_ID, null, null);

        GameSession result = gameService.executeTurn(sessionId, userMove, playerX);

        assertThat(result).isNotNull();
        // После хода игрока X, должен сходить ИИ (O)
        // Проверяем, что на поле есть хотя бы один ноль (ход ИИ)
        int[][] resultMap = result.getGameMap().getMap();
        boolean hasZero = false;
        for (int[] row : resultMap) {
            for (int cell : row) {
                if (cell == CellType.ZERO.getValue()) {
                    hasZero = true;
                    break;
                }
            }
        }
        assertThat(hasZero).isTrue();
        Mockito.verify(gameRepository, Mockito.times(1)).save(result);
    }
}