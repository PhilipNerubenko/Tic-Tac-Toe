package org.example.web.model;

/**
 * DTO для хода игрока.
 * <p>
 * Содержит только координаты хода и состояние игрового поля.
 * Не включает чувствительные данные сессии (статус, победитель и т.д.).
 *
 * @param gameMap состояние игрового поля после хода.
 */
public record MoveRequest(GameMapDTO gameMap) {
}
