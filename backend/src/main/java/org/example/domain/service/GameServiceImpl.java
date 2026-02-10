package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.domain.repository.GameRepository;

import java.util.Optional;

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
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int r = 0; r < map.getSize(); r++) {
            for (int c = 0; c < map.getSize(); c++) {
                if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                    // Симуляция хода
                    map.setCellValue(r, c, CellType.ZERO);
                    int score = minimax(map, 0, false);
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
            session.getGameMap().setCellValue(bestMove[0], bestMove[1], CellType.ZERO);
            session.setStatus(checkGameStatus(session.getGameMap()));
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
    public boolean validateMapIntegrity(GameSession gameSession, GameMap gameMap) {
        Optional<GameSession> savedSessionOpt = repository.findById(gameSession.getId());

        if (savedSessionOpt.isEmpty()) {
            return false;
        }

        GameSession savedSession = savedSessionOpt.get();
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

                if (oldMap[i][j] == CellType.EMPTY.getValue() && newMap[i][j] == CellType.CROSS.getValue()) {
                    newMoves++;
                }
            }
        }
        return newMoves == 1; // Ход валиден, если добавился только 1 крестик
    }

    /**
     * Определяет состояние игры на текущем поле.
     * Проверяет все линии (горизонтали, вертикали, диагонали) на наличие победителя.
     */
    @Override
    public GameStatus checkGameStatus(GameMap gameMap) {
        int size = gameMap.getSize();
        int[][] map = gameMap.getMap();

        for (int i = 0; i < size; i++) {
            if (checkLine(map, i, 0, 0, 1)) return getWinnerStatus(map[i][0]);
            if (checkLine(map, 0, i, 1, 0)) return getWinnerStatus(map[0][i]);
        }

        if (checkLine(map, 0, 0, 1, 1)) return getWinnerStatus(map[0][0]);
        if (checkLine(map, 0, size - 1, 1, -1)) return getWinnerStatus(map[0][size - 1]);

        boolean hasEmpty = false;
        for (int[] row : map) {
            for (int cell : row) {
                if (cell == CellType.EMPTY.getValue()) {
                    hasEmpty = true;
                    break;
                }
            }
        }

        return hasEmpty ? GameStatus.PLAYING : GameStatus.DRAW;
    }

    /**
     * Рекурсивный алгоритм поиска оптимального решения.
     * * @param isMaximizing true, если текущий ход за ИИ (максимизация),
     * false, если за человека (минимизация).
     * @return вес хода (чем больше, тем выгоднее для ИИ).
     */
    private int minimax(GameMap map, int depth, boolean isMaximizing) {
        GameStatus status = checkGameStatus(map);

        // Веса побед корректируются глубиной (depth), чтобы ИИ выбирал быстрейший путь к победе
        if (status == GameStatus.ZERO_WIN) return 10 - depth;
        if (status == GameStatus.CROSS_WIN) return depth - 10;
        if (status == GameStatus.DRAW) return 0;

        // Ограничение глубины для оптимизации производительности
        if (depth >= 4) {
            return evaluateBoard(map);
        }

        int bestScore;
        if (isMaximizing) {
            bestScore = Integer.MIN_VALUE;
            for (int r = 0; r < map.getSize(); r++) {
                for (int c = 0; c < map.getSize(); c++) {
                    if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                        map.setCellValue(r, c, CellType.ZERO);
                        bestScore = Math.max(bestScore, minimax(map, depth + 1, false));
                        map.setCellValue(r, c, CellType.EMPTY);
                    }
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (int r = 0; r < map.getSize(); r++) {
                for (int c = 0; c < map.getSize(); c++) {
                    if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                        map.setCellValue(r, c, CellType.CROSS);
                        bestScore = Math.min(bestScore, minimax(map, depth + 1, true));
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
    private int evaluateBoard(GameMap map) {
        int score = 0;
        int size = map.getSize();
        int[][] matrix = map.getMap();

        for (int i = 0; i < size; i++) {
            score += evaluateLine(getLine(matrix, i, 0, 0, 1, size));
            score += evaluateLine(getLine(matrix, 0, i, 1, 0, size));
        }

        score += evaluateLine(getLine(matrix, 0, 0, 1, 1, size));
        score += evaluateLine(getLine(matrix, 0, size - 1, 1, -1, size));

        return score;
    }

    /**
     * Рассчитывает вес конкретной линии.
     * Использует экспоненциальную шкалу оценки (10^n) для приоритезации линий с большим числом своих знаков.
     */
    private int evaluateLine(int[] line) {
        int zeros = 0;
        int crosses = 0;

        for (int cell : line) {
            if (cell == CellType.ZERO.getValue()) zeros++;
            else if (cell == CellType.CROSS.getValue()) crosses++;
        }

        if (zeros > 0 && crosses > 0) return 0; // Линия заблокирована
        if (zeros > 0) return (int) Math.pow(10, zeros - 1);
        if (crosses > 0) return -(int) Math.pow(10, crosses - 1);

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

    private GameStatus getWinnerStatus(int cellValue) {
        return (cellValue == CellType.CROSS.getValue()) ? GameStatus.CROSS_WIN : GameStatus.ZERO_WIN;
    }
}