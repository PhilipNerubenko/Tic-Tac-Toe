package org.example.web.mapper;

import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.web.model.GameMapDTO;
import org.example.web.model.GameSessionDTO;
import org.example.web.model.GameStatusDTO;

/**
 * Компонент-преобразователь (Mapper) для веб-уровня.
 * <p>
 * Преобразует внутренние доменные модели в объекты передачи данных (DTO) и обратно.
 * Это гарантирует, что изменения во внутренней логике игры не "сломают" API
 * для фронтенда или внешних потребителей.
 */
public class GameMapperDTO {

    /**
     * Конструктор по умолчанию.
     */
    private GameMapperDTO() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Преобразует доменную сессию в формат DTO для отправки клиенту по HTTP.
     *
     * @param session доменная модель сессии.
     * @return объект {@link GameSessionDTO} для JSON-ответа.
     */
    public static GameSessionDTO toDTO(GameSession session) {
        if (session == null) return null;

        return new GameSessionDTO(
                session.getId(),
                toWebMap(session.getGameMap()),
                GameStatusDTO.valueOf(session.getStatus().name())
        );
    }

    /**
     * Преобразует полученные от клиента данные (DTO) обратно в доменную модель.
     *
     * @param dto данные сессии, полученные из JSON-запроса.
     * @return доменная модель {@link GameSession}.
     */
    public static GameSession toDomain(GameSessionDTO dto) {
        if (dto == null) return null;

        return new GameSession(
                dto.getId(),
                toDomainMap(dto.getGameMap()),
                GameStatus.valueOf(dto.getStatus().name())
        );
    }

    /**
     * Создает копию игрового поля в формате DTO.
     * Выполняет глубокое копирование массива для обеспечения безопасности данных.
     */
    private static GameMapDTO toWebMap(GameMap domainMap) {
        int[][] rawMap = domainMap.getMap();
        int[][] copy = new int[domainMap.getSize()][domainMap.getSize()];
        for (int i = 0; i < domainMap.getSize(); i++) {
            copy[i] = rawMap[i].clone();
        }
        return new GameMapDTO(copy, domainMap.getSize());
    }

    /**
     * Преобразует DTO игрового поля в доменную структуру.
     */
    private static GameMap toDomainMap(GameMapDTO dtoMap) {
        int size = dtoMap.getSize();
        int[][] source = dtoMap.getMap();

        int[][] target = new int[size][size];
        for (int i = 0; i < size; i++) {
            target[i] = source[i].clone();
        }

        return new GameMap(target, size);
    }
}