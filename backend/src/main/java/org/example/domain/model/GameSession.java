package org.example.domain.model;

import java.util.UUID;

/**
 * Основная доменная модель игровой сессии "Крестиков-ноликов".
 * <p>
 * Класс управляет жизненным циклом одной игры, связывая уникальный
 * идентификатор сессии, игровое поле и текущий прогресс игры.
 */
public class GameSession {

    /** Уникальный идентификатор текущей игровой сессии */
    private final UUID id;

    /** Доменная модель игрового поля */
    private final GameMap map;

    /** Текущий статус игры (в процессе, победа X, победа O или ничья) */
    private GameStatus status;

    /**
     * Создает новую игровую сессию с уникальным ID.
     * По умолчанию устанавливает статус {@link GameStatus#PLAYING}.
     *
     * @param map инициализированное игровое поле (например, 3x3).
     */
    public GameSession(GameMap map) {
        this.id = UUID.randomUUID();
        this.map = map;
        this.status = GameStatus.PLAYING;
    }

    /**
     * Восстанавливает существующую игровую сессию.
     * Используется для загрузки данных из репозитория.
     *
     * @param id     UUID сессии.
     * @param map    объект игрового поля.
     * @param status актуальный статус игры.
     */
    public GameSession(UUID id, GameMap map, GameStatus status) {
        this.id = id;
        this.map = map;
        this.status = status;
    }

    /**
     * Возвращает уникальный идентификатор сессии.
     * @return уникальный идентификатор сессии.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает объект игрового поля.
     * @return объект игрового поля для совершения ходов.
     */
    public GameMap getGameMap() {
        return map;
    }

    /**
     * Возвращает текущий статус игры.
     * @return текущий статус игры.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Изменяет статус игры.
     * Вызывается сервисом при обнаружении победителя или окончании ходов.
     *
     * @param status новый статус сессии.
     */
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Проверяет, завершена ли игра.
     * @return {@code true}, если статус отличен от {@link GameStatus#PLAYING}.
     */
    public boolean isGameOver() {
        return status != GameStatus.PLAYING;
    }
}