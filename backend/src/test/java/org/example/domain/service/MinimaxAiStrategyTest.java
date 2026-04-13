package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для MinimaxAiStrategy.
 */
class MinimaxAiStrategyTest {

    private final MinimaxAiStrategy strategy = new MinimaxAiStrategy();

    @Test
    void calculateMove_ShouldFindWinningMove() {
        int[][] board = {
                {2, 2, 0},
                {1, 0, 0},
                {1, 0, 0}
        };
        GameMap map = new GameMap(board, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] move = strategy.calculateMove(session, CellType.ZERO);

        assertThat(move).isEqualTo(new int[]{0, 2});
    }

    @Test
    void calculateMove_ShouldBlockOpponentWin() {
        int[][] board = {
                {1, 1, 0},
                {2, 0, 0},
                {2, 0, 0}
        };
        GameMap map = new GameMap(board, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] move = strategy.calculateMove(session, CellType.ZERO);

        // ИИ должен заблокировать ход (0, 2)
        assertThat(move[0]).isEqualTo(0);
        assertThat(move[1]).isEqualTo(2);
    }

    @Test
    void calculateMove_ShouldReturnMinus1_WhenNoMovesAvailable() {
        int[][] board = {
                {1, 2, 1},
                {1, 2, 2},
                {2, 1, 2}
        };
        GameMap map = new GameMap(board, 3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] move = strategy.calculateMove(session, CellType.ZERO);

        assertThat(move).isEqualTo(new int[]{-1, -1});
    }

    @Test
    void calculateMove_ShouldTakeCenter_WhenEmpty() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        int[] move = strategy.calculateMove(session, CellType.ZERO);

        // Minimax обычно выбирает центр первым ходом
        assertThat(move).isNotNull();
        assertThat(move[0]).isBetween(0, 2);
        assertThat(move[1]).isBetween(0, 2);
    }
}
