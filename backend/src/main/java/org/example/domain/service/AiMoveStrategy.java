package org.example.domain.service;

import org.example.domain.model.CellType;
import org.example.domain.model.GameSession;

/**
 * Стратегия хода ИИ.
 * <p>
 * Позволяет подменять алгоритмы ИИ без изменения основной логики игры.
 */
public interface AiMoveStrategy {

    /**
     * Рассчитывает координаты следующего хода ИИ.
     *
     * @param session текущая игровая сессия.
     * @param aiSymbol символ, за который играет ИИ (CROSS или ZERO).
     * @return массив из двух элементов: {@code [row, col]}.
     */
    int[] calculateMove(GameSession session, CellType aiSymbol);
}
