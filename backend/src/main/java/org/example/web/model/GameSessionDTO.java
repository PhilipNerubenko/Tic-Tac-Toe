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

    /**
     * Конструктор без параметров.
     * Необходим для корректной работы десериализаторов JSON (например, Jackson).
     */
    public GameSessionDTO() {}

    /**
     * Создает заполненный объект сессии для передачи в ответе API.
     *
     * @param id      уникальный идентификатор.
     * @param gameMap данные поля.
     * @param status  текущий статус.
     */
    public GameSessionDTO(UUID id, GameMapDTO gameMap, GameStatusDTO status) {
        this.id = id;
        this.gameMap = gameMap;
        this.status = status;
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
     * Возвращает текущий статус игры.
     * @return статус игры (например, "PLAYING", "DRAW").
     */
    public GameStatusDTO getStatus() {
        return status;
    }

    /**
     * Устанавливает объект игрового поля.
     * @param gameMap объект поля для установки.
     */
    public void setGameMap(GameMapDTO gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Устанавливает текущий статус игры.
     * @param status объект статуса для установки.
     */
    public void setStatus(GameStatusDTO status) {
        this.status = status;
    }
}