package org.example.domain.service;

import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;

/**
 * Интерфейс игрового сервиса, определяющий правила и механику "Крестиков-ноликов".
 * <p>
 * Содержит методы для вычисления ходов, проверки корректности игрового поля
 * и определения текущего состояния игры (победа, ничья, продолжение).
 */
public interface GameService {

    /**
     * Рассчитывает координаты следующего оптимального хода (например, для ИИ).
     *
     * @param gameSession текущая игровая сессия.
     * @return массив из двух элементов: {@code [row, col]}, где row — строка, col — столбец.
     */
    int[] getNextMove(GameSession gameSession);

    /**
     * Проверяет целостность и валидность игрового поля в контексте сессии.
     * <p>
     * Сверяет количество совершенных ходов, отсутствие конфликтующих меток
     * и соответствие текущего состояния поля правилам игры.
     *
     * @param gameSessionEntity объект сессии.
     * @param gameMap            состояние игрового поля для проверки.
     * @return {@code true}, если поле валидно и не нарушает логику игры.
     */
    boolean validateMapIntegrity(GameSession gameSessionEntity, GameMap gameMap);

    /**
     * Анализирует игровое поле и определяет текущий статус игры.
     * <p>
     * Метод проверяет все возможные комбинации (горизонтали, вертикали, диагонали)
     * для выявления победителя, а также проверяет поле на наличие ничьей.
     *
     * @param gameMapEntity состояние поля для анализа.
     * @return {@link GameStatus} (PLAYING, CROSS_WIN, ZERO_WIN или DRAW).
     */
    GameStatus checkGameStatus(GameMap gameMapEntity);
}