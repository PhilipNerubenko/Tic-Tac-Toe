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
     * Конструктор по умолчанию запрещен.
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

        GameSessionDTO dto = new GameSessionDTO(
                session.getId(),
                toWebMap(session.getGameMap()),
                toWebStatus(session.getStatus()),
                session.getPlayerX(),
                session.getPlayerO(),
                session.getCurrentPlayer(),
                session.getWinner()
        );
        
        // Устанавливаем время последнего действия
        dto.setLastActiveAt(session.getLastActiveAt());
        
        return dto;
    }

    /**
     * Преобразует полученные от клиента данные (DTO) обратно в доменную модель.
     *
     * @param dto данные сессии, полученные из JSON-запроса.
     * @return доменная модель {@link GameSession}.
     */
    public static GameSession toDomain(GameSessionDTO dto) {
        if (dto == null) return null;

        // Используем полный конструктор GameSession для восстановления состояния
        GameSession session = new GameSession(
                dto.getId(),
                toDomainMap(dto.getGameMap()),
                toDomainStatus(dto.getStatus()),
                dto.getPlayerX(),
                dto.getPlayerO(),
                dto.getCurrentPlayer(),
                dto.getWinner()
        );
        
        // Устанавливаем время последнего действия
        session.setLastActiveAt(dto.getLastActiveAt());
        
        return session;
    }

    private static GameMapDTO toWebMap(GameMap domainMap) {
        if (domainMap == null) return null;
        int[][] rawMap = domainMap.getMap();
        int[][] copy = new int[domainMap.getSize()][domainMap.getSize()];
        for (int i = 0; i < domainMap.getSize(); i++) {
            copy[i] = rawMap[i].clone();
        }
        return new GameMapDTO(copy, domainMap.getSize());
    }

    private static GameMap toDomainMap(GameMapDTO dtoMap) {
        if (dtoMap == null) return null;
        int size = dtoMap.getSize();
        int[][] source = dtoMap.getMap();
        int[][] target = new int[size][size];
        for (int i = 0; i < size; i++) {
            target[i] = source[i].clone();
        }
        return new GameMap(target, size);
    }

    private static GameStatusDTO toWebStatus(GameStatus status) {
        if (status == null) return null;
        return GameStatusDTO.valueOf(status.name());
    }

    private static GameStatus toDomainStatus(GameStatusDTO dtoStatus) {
        if (dtoStatus == null) return null;
        return GameStatus.valueOf(dtoStatus.name());
    }
}