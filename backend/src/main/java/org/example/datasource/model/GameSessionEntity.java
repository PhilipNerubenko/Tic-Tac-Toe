package org.example.datasource.model;

import java.util.UUID;

/**
 * Сущность игровой сессии для хранения в базе данных.
 * <p>
 * Объединяет в себе идентификатор сессии, состояние игрового поля
 * и текущий статус игры (в процессе, завершена и т.д.).
 */
public class GameSessionEntity {

    /** Уникальный идентификатор игровой сессии */
    private final UUID id;

    /** Данные игрового поля */
    private final GameMapEntity map;

    /** Текущее состояние игры */
    private GameStatusEntity status;

    /**
     * Конструктор для создания новой игры.
     * Автоматически генерирует уникальный идентификатор и устанавливает
     * статус {@link GameStatusEntity#PLAYING}.
     *
     * @param map инициализированное игровое поле.
     */
    public GameSessionEntity(GameMapEntity map) {
        this.id = UUID.randomUUID();
        this.map = map;
        this.status = GameStatusEntity.PLAYING;
    }

    /**
     * Конструктор для восстановления существующей сессии (например, при загрузке из БД).
     *
     * @param id     существующий идентификатор сессии.
     * @param map    состояние поля.
     * @param status текущий статус игры.
     */
    public GameSessionEntity(UUID id, GameMapEntity map, GameStatusEntity status) {
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
     * Возвращает игровое поле.
     * @return объект игрового поля.
     */
    public GameMapEntity getGameMap() {
        return map;
    }

    /**
     * Возвращает текущий статус игры.
     * @return текущий статус сессии.
     */
    public GameStatusEntity getStatus() {
        return status;
    }

    /**
     * Обновляет текущий статус игры.
     * @param status новый статус (например, победа одного из игроков).
     */
    public void setStatus(GameStatusEntity status) {
        this.status = status;
    }
}