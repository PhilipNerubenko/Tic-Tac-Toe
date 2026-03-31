package org.example.domain.service;

import org.example.domain.exception.GameNotFoundException;
import org.example.domain.exception.IntegrityViolationException;
import org.example.domain.exception.NotYourTurnException;
import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Основная реализация игрового сервиса.
 * <p>
 * Класс содержит логику искусственного интеллекта на базе алгоритма Minimax,
 * механизмы валидации целостности игрового поля и алгоритмы проверки условий победы.
 */
public class GameServiceImpl implements GameService {
    private final GameRepository repository;

    /**
     * Конструктор для создания экземпляра сервиса.
     * <p>
     * Используется Spring для внедрения зависимости репозитория,
     * через который сервис будет сохранять и загружать состояние игр.
     *
     * @param repository репозиторий, обеспечивающий доступ к хранилищу сессий.
     */
    public GameServiceImpl(GameRepository repository) {
        this.repository = repository;
    }

    /**
     * Рассчитывает и выполняет ход ИИ (нолики).
     * Использует перебор всех возможных ходов и оценку их веса через алгоритм Minimax.
     *
     * @param session текущая игровая сессия
     * @return массив {@code [row, col]} с координатами выбранного хода
     */
    @Override
    public int[] getNextMove(GameSession session) {
        GameMap map = session.getGameMap();
        
        // Определяем символ ИИ на основе того, за какого игрока (X или O) играет ИИ
        CellType computerSymbol = session.getPlayerO().equals(GameSession.AI_PLAYER_ID) ? CellType.ZERO : CellType.CROSS;

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int r = 0; r < map.getSize(); r++) {
            for (int c = 0; c < map.getSize(); c++) {
                if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                    // Симуляция хода
                    map.setCellValue(r, c, computerSymbol);
                    int score = minimax(map, 0, false, computerSymbol);
                    map.setCellValue(r, c, CellType.EMPTY); // Откат хода

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = r;
                        bestMove[1] = c;
                    }
                }
            }
        }

        if (bestMove[0] != -1) {
            session.getGameMap().setCellValue(bestMove[0], bestMove[1], computerSymbol);
            updateGameProgress(session);
        }

        return bestMove;
    }

    /**
     * Проверяет корректность хода пользователя.
     * <p>
     * Условия валидности:
     * 1. Сессия существует в репозитории.
     * 2. Старые ходы не были изменены или затерты.
     * 3. Добавлен ровно один новый ход (крестик).
     */
    @Override
    @Transactional
    public boolean validateMapIntegrity(GameSession originalSession, GameMap newMap) {
        // Проверяем, что игра в процессе и ход ожидается
        if (originalSession.getStatus() != GameStatus.PLAYER_TURN) {
            return false;
        }

        // Проверяем, что размеры совпадают
        if (newMap.getSize() != originalSession.getGameMap().getSize()) {
            return false;
        }

        // Определяем символ, который должен поставить текущий игрок (из originalSession)
        CellType expectedSymbol = originalSession.getCurrentPlayer().equals(originalSession.getPlayerX())
                ? CellType.CROSS
                : CellType.ZERO;

        int[][] oldMap = originalSession.getGameMap().getMap();
        int[][] newMapArray = newMap.getMap();
        int size = originalSession.getGameMap().getSize();

        int newMoves = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Запрещено менять уже установленные знаки
                if (oldMap[i][j] != CellType.EMPTY.getValue() && oldMap[i][j] != newMapArray[i][j]) {
                    return false;
                }

                if (oldMap[i][j] == CellType.EMPTY.getValue() && newMapArray[i][j] != CellType.EMPTY.getValue()) {
                    // Игрок должен ходить только своим символом
                    if (newMapArray[i][j] != expectedSymbol.getValue()) {
                        return false;
                    }
                    newMoves++;
                }
            }
        }
        return newMoves == 1; // Ход валиден, если добавился только 1 знак
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
        } else if (result == 3) { // 3 — это ничья
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

        return hasEmpty ? 0 : 3;
    }

    /**
     * Определяет состояние игры на текущем поле.
     * Проверяет все линии (горизонтали, вертикали, диагонали) на наличие победителя.
     */
    @Override
    public GameStatus checkGameStatus(GameMap gameMap) {
        int result = evaluateInternalStatus(gameMap);
        if (result == 3) return GameStatus.DRAW;
        if (result != 0) return GameStatus.VICTORY;
        return GameStatus.PLAYER_TURN; 
    }

    @Override
    @Transactional
    public GameSession executeTurn(UUID id, GameSession userMove, UUID authenticatedUserId) {
        // 1. Загружаем оригинальную сессию из БД (источник истины)
        GameSession originalSession = repository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));

        // 2. Проверка авторизации: текущий игрок в сессии должен совпадать с аутентифицированным пользователем
        if (!originalSession.getCurrentPlayer().equals(authenticatedUserId)) {
            throw new NotYourTurnException("Вы не можете ходить, сейчас очередь другого игрока");
        }

        // 3. Валидация целостности карты (проверяем, что ход сделан тем игроком, который должен ходить, и что добавлен ровно один новый символ)
        if (!validateMapIntegrity(originalSession, userMove.getGameMap())) {
            throw new IntegrityViolationException();
        }

        // 3. Обновляем карту в originalSession, копируя значения из userMove.getGameMap()
        GameMap newMap = userMove.getGameMap();
        int size = newMap.getSize();
        int[][] newMapArray = newMap.getMap();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = newMapArray[i][j];
                CellType cellType = (value == 0) ? CellType.EMPTY : (value == 1) ? CellType.CROSS : CellType.ZERO;
                originalSession.getGameMap().setCellValue(i, j, cellType);
            }
        }

        // 4. Обновляем прогресс игры (проверка победы, ничьей, переключение хода)
        updateGameProgress(originalSession);

        // 5. Если игра не завершена и теперь ход ИИ, делаем ход ИИ
        if (!originalSession.isGameOver() && originalSession.getCurrentPlayer().equals(GameSession.AI_PLAYER_ID)) {
            getNextMove(originalSession);
        }

        // 6. Обновляем время последнего действия
        originalSession.updateLastActiveAt();
        
        // 7. Сохраняем обновленную сессию
        repository.save(originalSession);

        return originalSession;
    }

    @Override
    @Transactional
    public GameSession createGame(int size, UUID creatorId, boolean vsAi) {
        GameMap newMap = new GameMap(size);
        GameSession newSession = new GameSession(newMap, creatorId, vsAi);
        repository.save(newSession);
        return newSession;
    }

    // @Override
    @Override
    @Transactional
    public GameSession joinPlayer(UUID sessionId, UUID guestId) {
        GameSession session = repository.findById(sessionId)
                .orElseThrow(() -> new org.example.domain.exception.GameNotFoundException(sessionId));

        // Check if it's an AI game
        if (session.getPlayerO() != null && session.getPlayerO().equals(GameSession.AI_PLAYER_ID)) {
            throw new org.example.domain.exception.GameDomainException("Cannot join AI game");
        }

        // Check if game is already full (has both players)
        if (session.getPlayerO() != null && !session.getPlayerO().equals(GameSession.AI_PLAYER_ID)) {
            throw new org.example.domain.exception.GameDomainException("Game is already full");
        }

        if (session.getStatus() != org.example.domain.model.GameStatus.WAITING_FOR_PLAYERS) {
            throw new org.example.domain.exception.GameDomainException("Game has already started");
        }

        // Join the opponent to the game
        session.joinOpponent(guestId);

        // Update last active time
        session.updateLastActiveAt();
        
        repository.save(session);
        return session;
    }

    @Override
    public java.util.Map<UUID, GameSession> getActiveGames() {
        return repository.findAll().entrySet().stream()
                .filter(entry -> {
                    GameSession session = entry.getValue();
                    // Include only multiplayer games that are waiting for players
                    // Check if it's not an AI game (playerO is not AI) and playerO is null (only one player joined)
                    boolean isMultiplayerWaitingGame =
                        session.getPlayerO() == null &&
                        session.getStatus() == org.example.domain.model.GameStatus.WAITING_FOR_PLAYERS &&
                        session.getPlayerX() != null; // Make sure there's at least one player
                    
                    return isMultiplayerWaitingGame;
                })
                .collect(java.util.stream.Collectors.toMap(
                    java.util.Map.Entry::getKey,
                    java.util.Map.Entry::getValue
                ));
    }
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Игра не найдена"));

    //     if (session.isVsAi()) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя присоединиться к игре с ИИ");
    //     }

    //     if (session.getGuestId() != null) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "В игре уже есть второй игрок");
    //     }

    //     if (session.getCreatorId().equals(guestId)) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вы не можете играть сами с собой");
    //     }

    //     // Устанавливаем второго игрока
    //     session.setPlayerO(guestId);
    //     // Опционально: меняем статус игры на IN_PROGRESS
    //     session.setStatus(GameStatus.PLAYER_TURN);

    //     return repository.save(session);
    // }

    /**
     * Рекурсивный алгоритм поиска оптимального решения.
     * * @param isMaximizing true, если текущий ход за ИИ (максимизация),
     * false, если за человека (минимизация).
     * @return вес хода (чем больше, тем выгоднее для ИИ).
     */
    private int minimax(GameMap map, int depth, boolean isMaximizing, CellType aiSymbol) {
        int status = evaluateInternalStatus(map);
        int aiVal = aiSymbol.getValue();
        CellType humanSymbol = (aiSymbol == CellType.CROSS) ? CellType.ZERO : CellType.CROSS;

        // Веса побед корректируются глубиной (depth), чтобы ИИ выбирал быстрейший путь к победе
        if (status == aiVal) return 10 - depth;
        if (status != 0 && status != 3) return depth - 10;
        if (status == 3) return 0;

        // Ограничение глубины для оптимизации производительности
        if (depth >= 4) {
            return evaluateBoard(map, aiSymbol);
        }

        int bestScore;
        if (isMaximizing) {
            bestScore = Integer.MIN_VALUE;
            for (int r = 0; r < map.getSize(); r++) {
                for (int c = 0; c < map.getSize(); c++) {
                    if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                        map.setCellValue(r, c, aiSymbol);
                        bestScore = Math.max(bestScore, minimax(map, depth + 1, false, aiSymbol));
                        map.setCellValue(r, c, CellType.EMPTY);
                    }
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (int r = 0; r < map.getSize(); r++) {
                for (int c = 0; c < map.getSize(); c++) {
                    if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                        map.setCellValue(r, c, humanSymbol);
                        bestScore = Math.min(bestScore, minimax(map, depth + 1, true, aiSymbol));
                        map.setCellValue(r, c, CellType.EMPTY);
                    }
                }
            }
        }
        return bestScore;
    }

    /**
     * Эвристическая оценка поля при достижении лимита глубины рекурсии.
     */
    private int evaluateBoard(GameMap map, CellType aiSymbol) {
        int score = 0;
        int size = map.getSize();
        int[][] matrix = map.getMap();

        for (int i = 0; i < size; i++) {
            score += evaluateLine(getLine(matrix, i, 0, 0, 1, size), aiSymbol);
            score += evaluateLine(getLine(matrix, 0, i, 1, 0, size), aiSymbol);
        }

        score += evaluateLine(getLine(matrix, 0, 0, 1, 1, size), aiSymbol);
        score += evaluateLine(getLine(matrix, 0, size - 1, 1, -1, size), aiSymbol);

        return score;
    }

    /**
     * Рассчитывает вес конкретной линии.
     * Использует экспоненциальную шкалу оценки (10^n) для приоритезации линий с большим числом своих знаков.
     */
    private int evaluateLine(int[] line, CellType aiSymbol) {
        int aiCount = 0;
        int humanCount = 0;

        for (int cell : line) {
            if (cell == aiSymbol.getValue()) aiCount++;
            else if (cell != CellType.EMPTY.getValue()) humanCount++;
        }

        if (aiCount > 0 && humanCount > 0) return 0; // Линия заблокирована
        if (aiCount > 0) return (int) Math.pow(10, aiCount - 1);
        if (humanCount > 0) return -(int) Math.pow(10, humanCount - 1);

        return 0;
    }

    /**
     * Извлекает массив ячеек по заданному вектору (направлению).
     */
    private int[] getLine(int[][] matrix, int startR, int startC, int dR, int dC, int size) {
        int[] line = new int[size];
        for (int i = 0; i < size; i++) {
            line[i] = matrix[startR + i * dR][startC + i * dC];
        }
        return line;
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
    @Override
    public java.util.Optional<GameSession> findById(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    @Transactional
    public GameSession findGameForUser(UUID gameId, UUID userId) {
        GameSession session = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        
        if (!session.isPlayer(userId)) {
            throw new org.example.domain.exception.GameDomainException("Пользователь не является участником игры");
        }
        
        return session;
    }
    
    @Override
    @Transactional
    public GameSession checkOpponentLeft(UUID gameId, UUID userId, long timeoutSeconds) {
        GameSession session = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        
        if (!session.isPlayer(userId)) {
            throw new org.example.domain.exception.GameDomainException("Пользователь не является участником игры");
        }
        
        // Проверяем, не завершена ли уже игра
        if (session.isGameOver() || session.getStatus() == GameStatus.OPPONENT_LEFT) {
            return session;
        }
        
        // Проверяем, прошло ли достаточно времени с последнего действия
        java.time.Instant lastActive = session.getLastActiveAt();
        if (lastActive != null) {
            long secondsSinceLastAction = java.time.Duration.between(lastActive, java.time.Instant.now()).getSeconds();
            
            if (secondsSinceLastAction > timeoutSeconds) {
                // Игрок покинул игру - определяем, кто именно
                UUID leftPlayer = session.getCurrentPlayer();
                UUID winnerId = null;
                
                if (leftPlayer != null) {
                    // Определяем победителя - это другой игрок
                    if (leftPlayer.equals(session.getPlayerX())) {
                        winnerId = session.getPlayerO();
                    } else {
                        winnerId = session.getPlayerX();
                    }
                    
                    // Не засчитываем победу, если победитель - это AI
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
}
