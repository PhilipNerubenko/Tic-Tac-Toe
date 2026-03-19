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

    /**  Идентификатор игрока, играющего за X */
    private UUID playerX;

    /**  Идентификатор игрока, играющего за O */
    private UUID playerO; 

    /**  Идентификатор игрока, который должен сделать ход */
    private UUID currentPlayer; 

    /**  Идентификатор победителя, если игра завершена */
    private UUID winner;

    /**
     * Создает новую игровую сессию с уникальным ID.
     * По умолчанию устанавливает статус {@link GameStatus#WAITING_FOR_PLAYERS}.
     *
     * @param map инициализированное игровое поле (например, 3x3).
     */
    public GameSession(GameMap map, UUID creatorId) {
        this.id = UUID.randomUUID();
        this.map = map;
        this.playerX = creatorId;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
    }

    /**
     * Восстанавливает существующую игровую сессию.
     * Используется для загрузки данных из репозитория.
     *
     * @param id     UUID сессии.
     * @param map    объект игрового поля.
     * @param status актуальный статус игры.
     * @param playerX UUID игрока, играющего за X.
     * @param playerO UUID игрока, играющего за O.
     * @param currentPlayer UUID игрока, который должен сделать ход.
     * @param winner UUID победителя, если игра завершена (может быть null).
     */
    public GameSession(UUID id, GameMap map, GameStatus status, 
                       UUID playerX, UUID playerO, UUID currentPlayer, UUID winner) {
        this.id = id;
        this.map = map;
        this.status = status;
        this.playerX = playerX;
        this.playerO = playerO;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
    }

    public void joinOpponent(UUID opponentId) {
        if (status != GameStatus.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Невозможно присоединиться к игре, которая уже началась.");
        }
        if (playerX.equals(opponentId)) {
            throw new IllegalArgumentException("Игрок уже является участником этой игры.");
        }
        this.playerO = opponentId;
        this.currentPlayer = playerX; // Игра начинается с игрока X
        this.status = GameStatus.PLAYER_TURN;
    }

    public void switchTurn() {
        if (status == GameStatus.PLAYER_TURN) {
            this.currentPlayer = (currentPlayer.equals(playerX)) ? playerO : playerX;
        }
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
     * Возвращает UUID игрока, играющего за X.
     * @return UUID игрока X, или null, если игрок еще не присоединился.
     */
    public UUID getPlayerX() {
        return playerX;
    }

    /**
     * Устанавливает UUID игрока, играющего за X.
     * @param playerX UUID игрока X, который создал игру или присоединился первым.
     */
    public void setPlayerX(UUID playerX) {
        this.playerX = playerX;
    }

    /**
     * Возвращает UUID игрока, играющего за O.
     * @return UUID игрока O, или null, если игрок еще не присоединился.
     */
    public UUID getPlayerO() {
        return playerO;
    }

    /**
     * Устанавливает UUID игрока, играющего за O.
     * @param playerO UUID игрока O, который присоединился к игре.
     */
    public void setPlayerO(UUID playerO) {
        this.playerO = playerO;
    }

    /**
     * Возвращает UUID игрока, который должен сделать ход.
     * @return UUID текущего игрока, или null, если игра не началась.
     */
    public UUID getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Устанавливает UUID игрока, который должен сделать ход.
     * Вызывается сервисом после каждого хода для переключения текущего игрока.
     *
     * @param currentPlayer UUID игрока, который должен сделать следующий ход.
     */
    public void setCurrentPlayer(UUID currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Возвращает UUID победителя, если игра завершена.
     * @return UUID победителя, или null, если игра не завершена или закончилась ничьей.
     */
    public UUID getWinner() {
        return winner;
    }

    /**
     * Устанавливает UUID победителя, если игра завершена.
     * Вызывается сервисом при обнаружении победителя.
     *
     * @param winner UUID игрока, который выиграл игру, или null в случае ничьей.
     */
    public void setWinner(UUID winner) {
        this.winner = winner;
    }

    /**
    * Проверяет, завершена ли игра победой одного из игроков или ничьей.
    * @return {@code true}, если статус игры - {@link GameStatus#VICTORY} или {@link GameStatus#DRAW}.
    */
    public boolean isGameOver() {
        return status == GameStatus.VICTORY || status == GameStatus.DRAW;
    }

    /**
     * Проверяет, ожидает ли игра ход от конкретного игрока.
     * @param playerId UUID игрока для проверки.
     * @return {@code true}, если статус игры - {@link GameStatus#PLAYER_TURN} и текущий игрок совпадает с переданным ID.
     */
    public boolean isWaitingForMoveFromPlayer(UUID playerId) {
        return status == GameStatus.PLAYER_TURN && playerId != null && playerId.equals(currentPlayer);
    }
}