package org.example.datasource.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность игровой сессии для хранения в базе данных.
 * <p>
 * Объединяет в себе идентификатор сессии, состояние игрового поля,
 * участников матча и текущий статус игры.
 */
@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {

    /** Уникальный идентификатор игровой сессии */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Данные игрового поля */
    @Embedded
    private GameMapEntity map;

    /** Текущее состояние игры */
    @Enumerated(EnumType.STRING)
    private GameStatusEntity status;

    /** Идентификатор игрока, играющего крестиками */
    @Column(name = "player_x_id")
    private UUID playerX;

    /** Идентификатор игрока, играющего ноликами */
    @Column(name = "player_o_id")
    private UUID playerO;

    /** Идентификатор игрока, чей сейчас ход */
    @Column(name = "current_player_id")
    private UUID currentPlayer;

    /** Идентификатор победителя */
    @Column(name = "winner_id")
    private UUID winner;

    /** Время последнего действия игрока */
    @Column(name = "last_active_at")
    private java.time.Instant lastActiveAt;

    /** Дата создания игры */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Конструктор по умолчанию, необходимый для JPA/Hibernate.
     */
    protected GameSessionEntity() {
    }

    /**
     * Конструктор для создания новой игры.
     * Автоматически генерирует уникальный идентификатор и устанавливает
     * начальный статус.
     *
     * @param map инициализированное игровое поле.
     */
    public GameSessionEntity(GameMapEntity map) {
        this.id = UUID.randomUUID();
        this.map = map;
        this.status = GameStatusEntity.WAITING_FOR_PLAYERS;
        this.lastActiveAt = java.time.Instant.now();
        this.createdAt = java.time.Instant.now();
    }

    /**
     * Полный конструктор для маппинга или восстановления сессии.
     */
    public GameSessionEntity(UUID id, GameMapEntity map, GameStatusEntity status,
                             UUID playerX, UUID playerO, UUID currentPlayer, UUID winner, Instant lastActiveAt,
                             Instant createdAt) {
        this.id = id;
        this.map = map;
        this.status = status;
        this.playerX = playerX;
        this.playerO = playerO;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.lastActiveAt = lastActiveAt;
        this.createdAt = createdAt;
    }

    /**
     * Возвращает уникальный идентификатор сессии.
     * @return идентификатор сессии.
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
     * @param status новый статус.
     */
    public void setStatus(GameStatusEntity status) {
        this.status = status;
    }

    /**
     * Возвращает идентификатор игрока, играющего крестиками.
     * @return идентификатор игрока X.
     */
    public UUID getPlayerX() {
        return playerX;
    }

    /**
     * Устанавливает идентификатор игрока, играющего крестиками.
     * @param playerX идентификатор игрока X.
     */
    public void setPlayerX(UUID playerX) {
        this.playerX = playerX;
    }

    /**
     * Возвращает идентификатор игрока, играющего ноликами.
     * @return идентификатор игрока O.
     */
    public UUID getPlayerO() {
        return playerO;
    }

    /**
     * Устанавливает идентификатор игрока, играющего ноликами.
     * @param playerO идентификатор игрока O.
     */
    public void setPlayerO(UUID playerO) {
        this.playerO = playerO;
    }

    /**
     * Возвращает идентификатор игрока, чей сейчас ход.
     * @return идентификатор текущего игрока.
     */ 
    public UUID getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Устанавливает идентификатор игрока, чей сейчас ход.
     * @param currentPlayer идентификатор текущего игрока.
     */
    public void setCurrentPlayer(UUID currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Возвращает идентификатор победителя.
     * @return идентификатор победителя.
     */
    public UUID getWinner() {
        return winner;
    }

    /**
     * Устанавливает идентификатор победителя.
     * @param winner идентификатор победителя.
     */
    public void setWinner(UUID winner) {
        this.winner = winner;
    }
    
    /**
     * Возвращает время последнего действия игрока.
     * @return время последнего действия
     */
    public java.time.Instant getLastActiveAt() {
        return lastActiveAt;
    }
    
    /**
     * Устанавливает время последнего действия игрока.
     * @param lastActiveAt время последнего действия
     */
    public void setLastActiveAt(java.time.Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}