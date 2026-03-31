package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;

/**
 * Реализация AI-стратегии на основе алгоритма Minimax.
 * <p>
 * Алгоритм Minimax перебирает все возможные ходы и оценивает их вес,
 * выбирая оптимальный ход для ИИ.
 */
public class MinimaxAiStrategy implements AiMoveStrategy {

    /** Константа для обозначения ничьей во внутренней оценке */
    private static final int DRAW_RESULT = 3;

    /** Максимальная глубина рекурсии для оптимизации производительности */
    private static final int MAX_DEPTH = 4;

    @Override
    public int[] calculateMove(GameSession session, CellType aiSymbol) {
        GameMap map = session.getGameMap();

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int r = 0; r < map.getSize(); r++) {
            for (int c = 0; c < map.getSize(); c++) {
                if (map.getMap()[r][c] == CellType.EMPTY.getValue()) {
                    // Симуляция хода
                    map.setCellValue(r, c, aiSymbol);
                    int score = minimax(map, 0, false, aiSymbol);
                    map.setCellValue(r, c, CellType.EMPTY); // Откат хода

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = r;
                        bestMove[1] = c;
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * Рекурсивный алгоритм поиска оптимального решения.
     *
     * @param map          игровое поле.
     * @param depth        текущая глубина рекурсии.
     * @param isMaximizing true, если текущий ход за ИИ (максимизация),
     *                     false, если за человека (минимизация).
     * @param aiSymbol     символ ИИ.
     * @return вес хода (чем больше, тем выгоднее для ИИ).
     */
    private int minimax(GameMap map, int depth, boolean isMaximizing, CellType aiSymbol) {
        int status = evaluateBoardStatus(map);
        int aiVal = aiSymbol.getValue();
        CellType humanSymbol = (aiSymbol == CellType.CROSS) ? CellType.ZERO : CellType.CROSS;

        // Веса побед корректируются глубиной (depth), чтобы ИИ выбирал быстрейший путь к победе
        if (status == aiVal) return 10 - depth;
        if (status != 0 && status != DRAW_RESULT) return depth - 10;
        if (status == DRAW_RESULT) return 0;

        // Ограничение глубины для оптимизации производительности (только для больших досок)
        if (map.getSize() > 3 && depth >= MAX_DEPTH) {
            return evaluateBoardHeuristic(map, aiSymbol);
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
     * Внутренняя оценка состояния поля.
     * Возвращает: 1 (Победа X), 2 (Победа O), 3 (Ничья), 0 (Игра продолжается)
     */
    private int evaluateBoardStatus(GameMap gameMap) {
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
     * Эвристическая оценка поля при достижении лимита глубины рекурсии.
     */
    private int evaluateBoardHeuristic(GameMap map, CellType aiSymbol) {
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
     * Использует экспоненциальную шкалу оценки (10^n) для приоритезации линий.
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
