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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация игрового сервиса.
 * <p>
 * Отвечает за управление игровыми сессиями: создание, присоединение,
 * выполнение ходов и проверку статуса. AI-логика делегирована {@link AiMoveStrategy}.
 */
public class GameServiceImpl implements GameService {

    /** Константа для обозначения ничьей во внутренней оценке */
    private static final int DRAW_RESULT = 3;

    private final GameRepository repository;
    private final AiMoveStrategy aiMoveStrategy;

    public GameServiceImpl(GameRepository repository, AiMoveStrategy aiMoveStrategy) {
        this.repository = repository;
        this.aiMoveStrategy = aiMoveStrategy;
    }

    @Override
    public int[] getNextMove(GameSession session) {
        CellType aiSymbol = session.getPlayerO().equals(GameSession.AI_PLAYER_ID) ? CellType.ZERO : CellType.CROSS;
        int[] move = aiMoveStrategy.calculateMove(session, aiSymbol);

        if (move[0] != -1) {
            session.getGameMap().setCellValue(move[0], move[1], aiSymbol);
            updateGameProgress(session);
        }

        return move;
    }

    @Override
    public boolean validateMapIntegrity(GameSession originalSession, GameMap newMap) {
        if (originalSession.getStatus() != GameStatus.PLAYER_TURN) {
            return false;
        }

        if (newMap.getSize() != originalSession.getGameMap().getSize()) {
            return false;
        }

        CellType expectedSymbol = originalSession.getCurrentPlayer().equals(originalSession.getPlayerX())
                ? CellType.CROSS
                : CellType.ZERO;

        int[][] oldMap = originalSession.getGameMap().getMap();
        int[][] newMapArray = newMap.getMap();
        int size = originalSession.getGameMap().getSize();

        int newMoves = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (oldMap[i][j] != CellType.EMPTY.getValue() && oldMap[i][j] != newMapArray[i][j]) {
                    return false;
                }

                if (oldMap[i][j] == CellType.EMPTY.getValue() && newMapArray[i][j] != CellType.EMPTY.getValue()) {
                    if (newMapArray[i][j] != expectedSymbol.getValue()) {
                        return false;
                    }
                    newMoves++;
                }
            }
        }
        return newMoves == 1;
    }

    @Override
    public GameStatus checkGameStatus(GameMap gameMap) {
        int result = evaluateInternalStatus(gameMap);
        if (result == DRAW_RESULT) return GameStatus.DRAW;
        if (result != 0) return GameStatus.VICTORY;
        return GameStatus.PLAYER_TURN;
    }

    @Override
    public GameSession executeTurn(UUID id, GameSession userMove, UUID authenticatedUserId) {
        GameSession originalSession = repository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));

        if (!originalSession.getCurrentPlayer().equals(authenticatedUserId)) {
            throw new NotYourTurnException("Вы не можете ходить, сейчас очередь другого игрока");
        }

        if (!validateMapIntegrity(originalSession, userMove.getGameMap())) {
            throw new IntegrityViolationException();
        }

        applyMapToSession(originalSession, userMove.getGameMap());
        updateGameProgress(originalSession);

        if (!originalSession.isGameOver() && originalSession.getCurrentPlayer().equals(GameSession.AI_PLAYER_ID)) {
            getNextMove(originalSession);
        }

        originalSession.updateLastActiveAt();
        repository.save(originalSession);

        return originalSession;
    }

    @Override
    public GameSession createGame(int size, UUID creatorId, boolean vsAi) {
        GameMap newMap = new GameMap(size);
        GameSession newSession = new GameSession(newMap, creatorId, vsAi);
        repository.save(newSession);
        return newSession;
    }

    @Override
    public GameSession joinPlayer(UUID sessionId, UUID guestId) {
        GameSession session = repository.findById(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));

        if (session.getPlayerO() != null && session.getPlayerO().equals(GameSession.AI_PLAYER_ID)) {
            throw new GameDomainException("Cannot join AI game");
        }

        if (session.getPlayerO() != null && !session.getPlayerO().equals(GameSession.AI_PLAYER_ID)) {
            throw new GameDomainException("Game is already full");
        }

        if (session.getStatus() != GameStatus.WAITING_FOR_PLAYERS) {
            throw new GameDomainException("Game has already started");
        }

        session.joinOpponent(guestId);
        session.updateLastActiveAt();
        repository.save(session);
        return session;
    }

    @Override
    public Map<UUID, GameSession> getActiveGames() {
        return repository.findAll().entrySet().stream()
                .filter(entry -> {
                    GameSession session = entry.getValue();
                    return session.getPlayerO() == null
                            && session.getStatus() == GameStatus.WAITING_FOR_PLAYERS
                            && session.getPlayerX() != null;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Optional<GameSession> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public GameSession findGameForUser(UUID gameId, UUID userId) {
        GameSession session = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (!session.isPlayer(userId)) {
            throw new GameDomainException("Пользователь не является участником игры");
        }

        return session;
    }

    @Override
    public GameSession checkOpponentLeft(UUID gameId, UUID userId, long timeoutSeconds) {
        GameSession session = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (!session.isPlayer(userId)) {
            throw new GameDomainException("Пользователь не является участником игры");
        }

        if (session.isGameOver() || session.getStatus() == GameStatus.OPPONENT_LEFT) {
            return session;
        }

        Instant lastActive = session.getLastActiveAt();
        if (lastActive != null) {
            long secondsSinceLastAction = Duration.between(lastActive, Instant.now()).getSeconds();

            if (secondsSinceLastAction > timeoutSeconds) {
                UUID leftPlayer = session.getCurrentPlayer();
                UUID winnerId = null;

                if (leftPlayer != null) {
                    winnerId = leftPlayer.equals(session.getPlayerX())
                            ? session.getPlayerO()
                            : session.getPlayerX();

                    if (winnerId != null && !winnerId.equals(GameSession.AI_PLAYER_ID)) {
                        session.setWinner(winnerId);
                    }
                }

                session.setStatus(GameStatus.OPPONENT_LEFT);
                session.setCurrentPlayer(null);
                repository.save(session);
            }
        }

        return session;
    }

    /**
     * Применяет новое состояние поля к сессии.
     */
    private void applyMapToSession(GameSession session, GameMap newMap) {
        int size = newMap.getSize();
        int[][] newMapArray = newMap.getMap();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = newMapArray[i][j];
                CellType cellType = (value == 0) ? CellType.EMPTY : (value == 1) ? CellType.CROSS : CellType.ZERO;
                session.getGameMap().setCellValue(i, j, cellType);
            }
        }
    }

    /**
     * Анализирует поле и обновляет состояние сессии (победитель, ничья, смена хода).
     */
    private void updateGameProgress(GameSession session) {
        int result = evaluateInternalStatus(session.getGameMap());

        if (result == CellType.CROSS.getValue() || result == CellType.ZERO.getValue()) {
            UUID winnerId = (result == CellType.CROSS.getValue()) ? session.getPlayerX() : session.getPlayerO();
            session.setWinner(winnerId);
            session.setStatus(GameStatus.VICTORY);
            session.setCurrentPlayer(null);
        } else if (result == DRAW_RESULT) {
            session.setStatus(GameStatus.DRAW);
            session.setCurrentPlayer(null);
        } else {
            session.switchTurn();
        }
    }

    /**
     * Внутренняя оценка состояния поля.
     * Возвращает: 1 (Победа X), 2 (Победа O), 3 (Ничья), 0 (Игра продолжается)
     */
    private int evaluateInternalStatus(GameMap gameMap) {
        int size = gameMap.getSize();
        int[][] map = gameMap.getMap();

        for (int i = 0; i < size; i++) {
            if (checkLine(map, i, 0, 0, 1)) return map[i][0];
            if (checkLine(map, 0, i, 1, 0)) return map[0][i];
        }

        if (checkLine(map, 0, 0, 1, 1)) return map[0][0];
        if (checkLine(map, 0, size - 1, 1, -1)) return map[0][size - 1];

        boolean hasEmpty = false;
        for (int[] row : map) {
            for (int cell : row) {
                if (cell == CellType.EMPTY.getValue()) {
                    hasEmpty = true;
                    break;
                }
            }
        }

        return hasEmpty ? 0 : DRAW_RESULT;
    }

    /**
     * Проверяет, заполнены ли все ячейки линии одним символом (не пустым).
     */
    private boolean checkLine(int[][] map, int startRow, int startCol, int dRow, int dCol) {
        int first = map[startRow][startCol];
        if (first == CellType.EMPTY.getValue()) return false;
        for (int i = 1; i < map.length; i++) {
            if (map[startRow + i * dRow][startCol + i * dCol] != first) return false;
        }
        return true;
    }
}
