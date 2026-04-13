package org.example.web.mapper;

import org.example.domain.model.CellType;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.web.model.GameMapDTO;
import org.example.web.model.GameSessionDTO;
import org.example.web.model.GameStatusDTO;
import org.example.web.model.MoveRequest;

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

        return new GameSessionDTO(
                session.getId(),
                toWebMap(session.getGameMap()),
                toWebStatus(session.getStatus()),
                session.getPlayerX(),
                session.getPlayerO(),
                session.getCurrentPlayer(),
                session.getWinner(),
                session.getLastActiveAt()
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
                toDomainStatus(dto.getStatus()),
                dto.getPlayerX(),
                dto.getPlayerO(),
                dto.getCurrentPlayer(),
                dto.getWinner(),
                dto.getLastActiveAt()
        );
    }

    /**
     * Преобразует MoveRequest в доменную модель GameMap.
     * Используется для обработки хода игрока.
     *
     * @param request запрос на ход.
     * @return доменная модель игрового поля.
     */
    public static GameMap toGameMap(MoveRequest request) {
        if (request == null || request.gameMap() == null) return null;
        return toDomainMap(request.gameMap());
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
        if (size <= 0 || source == null || source.length != size) {
            throw new IllegalArgumentException("Invalid game map payload");
        }
        int[][] target = new int[size][size];
        for (int i = 0; i < size; i++) {
             if (source[i] == null || source[i].length != size) {
                throw new IllegalArgumentException("Invalid game map payload");
            }
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