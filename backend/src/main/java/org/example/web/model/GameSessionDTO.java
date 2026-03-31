package org.example.web.model;

import java.util.UUID;

/**
 * Объект передачи данных (DTO) для игровой сессии.
 * <p>
 * Представляет собой агрегированную информацию об игре, которая
 * передается между клиентом и сервером через REST API.
 */
public class GameSessionDTO {

    /** Уникальный идентификатор сессии, используемый в URL запросов */
    private UUID id;

    /** Текущее состояние игрового поля */
    private GameMapDTO gameMap;

    /** Текущий статус игры в формате, понятном для веб-интерфейса */
    private GameStatusDTO status;

    private UUID playerX; // Добавь это
    private UUID playerO; // Добавь это

    /** Идентификатор игрока, чей сейчас ход */
    private UUID currentPlayer;

    /** Идентификатор победителя (заполняется при завершении игры) */
    private UUID winner;

    /** Время последнего действия игрока */
    private java.time.Instant lastActiveAt;

    /**
     * Конструктор без параметров.
     * Необходим для корректной работы десериализаторов JSON (например, Jackson).
     */
    public GameSessionDTO() {}

    /**
     * Создает заполненный объект сессии для передачи в ответе API.
     *
     * @param id            уникальный идентификатор сессии.
     * @param gameMap       данные игрового поля.
     * @param status        текущий статус игры.
     * @param playerX       ID игрока X.
     * @param playerO       ID игрока O.
     * @param currentPlayer ID игрока, который должен ходить.
     * @param winner        ID победителя (может быть null).
     * @param lastActiveAt  время последнего действия игрока (может быть null).
     */
    public GameSessionDTO(UUID id, GameMapDTO gameMap, GameStatusDTO status, UUID playerX, UUID playerO, UUID currentPlayer, UUID winner, java.time.Instant lastActiveAt) {
        this.id = id;
        this.gameMap = gameMap;
        this.status = status;
        this.playerX = playerX;
        this.playerO = playerO;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.lastActiveAt = lastActiveAt != null ? lastActiveAt : java.time.Instant.now();
    }

    /**
     * Возвращает идентификатор сессии.
     * @return идентификатор сессии.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор сессии.
     * @param id идентификатор для установки.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Возвращает объект игрового поля.
     * @return DTO игрового поля.
     */
    public GameMapDTO getGameMap() {
        return gameMap;
    }

    /**
     * Устанавливает объект игрового поля.
     * @param gameMap объект поля для установки.
     */
    public void setGameMap(GameMapDTO gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Возвращает текущий статус игры.
     * @return статус игры (например, "PLAYER_TURN", "VICTORY").
     */
    public GameStatusDTO getStatus() {
        return status;
    }

    /**
     * Устанавливает текущий статус игры.
     * @param status объект статуса для установки.
     */
    public void setStatus(GameStatusDTO status) {
        this.status = status;
    }

    public UUID getPlayerX() {
        return playerX;
    }

    public void setPlayerX(UUID playerX) {
        this.playerX = playerX;
    }

    public UUID getPlayerO() {
        return playerO;
    }

    public void setPlayerO(UUID playerO) {
        this.playerO = playerO;
    }

    /**
     * Возвращает идентификатор игрока, чей сейчас ход.
     * @return UUID текущего игрока.
     */
    public UUID getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Устанавливает идентификатор текущего игрока.
     * @param currentPlayer UUID игрока для установки.
     */
    public void setCurrentPlayer(UUID currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Возвращает идентификатор победителя.
     * @return UUID победителя или null, если игра не окончена.
     */
    public UUID getWinner() {
        return winner;
    }

    /**
     * Устанавливает идентификатор победителя.
     * @param winner UUID победившего игрока.
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
}