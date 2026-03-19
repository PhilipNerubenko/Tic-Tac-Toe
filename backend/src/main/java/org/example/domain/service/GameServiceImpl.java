package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;

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
        
        // Динамически определяем символ ИИ
        boolean isComputerX = session.getCurrentPlayer() != null && session.getCurrentPlayer().equals(session.getPlayerX());
        CellType computerSymbol = isComputerX ? CellType.CROSS : CellType.ZERO;

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
            repository.save(session);
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
    public boolean validateMapIntegrity(GameSession gameSession, GameMap gameMap, UUID requesterId) {
        Optional<GameSession> savedSessionOpt = repository.findById(gameSession.getId());

        if (savedSessionOpt.isEmpty()) {
            return false;
        }

        GameSession savedSession = savedSessionOpt.get();

        // Проверка: очередь ли этого игрока?
        if (!savedSession.isWaitingForMoveFromPlayer(requesterId)) {
            return false;
        }

        CellType expectedSymbol = requesterId.equals(savedSession.getPlayerX()) ? CellType.CROSS : CellType.ZERO;

        int[][] oldMap = savedSession.getGameMap().getMap();
        int[][] newMap = gameMap.getMap();
        int size = savedSession.getGameMap().getSize();

        int newMoves = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Запрещено менять уже установленные знаки
                if (oldMap[i][j] != CellType.EMPTY.getValue() && oldMap[i][j] != newMap[i][j]) {
                    return false;
                }

                if (oldMap[i][j] == CellType.EMPTY.getValue() && newMap[i][j] != CellType.EMPTY.getValue()) {
                    // Игрок должен ходить только своим символом
                    if (newMap[i][j] != expectedSymbol.getValue()) {
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
}