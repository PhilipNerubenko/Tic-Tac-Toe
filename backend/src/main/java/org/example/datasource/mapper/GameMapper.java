package org.example.datasource.mapper;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Компонент-преобразователь (Mapper) между доменной моделью и сущностями базы данных.
 * <p>
 * Служит для обеспечения архитектурной изоляции: изменения в структуре БД
 * не должны напрямую влиять на бизнес-логику приложения.
 */
public class GameMapper {

    /**
     * Конструктор по умолчанию запрещен.
     */
    private GameMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Преобразует сущность БД в объект доменной области.
     * Используется при извлечении данных из репозитория.
     *
     * @param entity сущность, полученная из источника данных.
     * @return объект {@link GameSession} или {@code null}, если входные данные отсутствуют.
     */
    public static GameSession toDomain(GameSessionEntity entity) {
        if (entity == null) return null;

        GameSession gameSession = new GameSession(
                entity.getId(),
                toMap(entity.getGameMap()),
                toStatus(entity.getStatus()),
                entity.getPlayerX(),
                entity.getPlayerO(),
                entity.getCurrentPlayer(),
                entity.getWinner()
        );
        
        // Устанавливаем время последнего действия
        gameSession.setLastActiveAt(entity.getLastActiveAt());
        
        return gameSession;
    }

    /**
     * Преобразует доменную модель сессии в сущность для сохранения в БД.
     *
     * @param domain объект доменной области.
     * @return сущность {@link GameSessionEntity} или {@code null}, если объект пуст.
     */
    public static GameSessionEntity toEntity(GameSession domain) {
        if (domain == null) return null;

        GameSessionEntity entity = new GameSessionEntity(
                domain.getId(),
                toMapEntity(domain.getGameMap()),
                toStatusEntity(domain.getStatus()),
                domain.getPlayerX(),
                domain.getPlayerO(),
                domain.getCurrentPlayer(),
                domain.getWinner()
        );
        
        // Устанавливаем время последнего действия
        entity.setLastActiveAt(domain.getLastActiveAt());
        
        return entity;
    }

    /**
     * Преобразует коллекцию сущностей в коллекцию доменных моделей.
     *
     * @param entities карта (Map), где ключ — UUID сессии, а значение — сущность БД.
     * @return карта с доменными моделями.
     */
    public static Map<UUID, GameSession> toDomainMap(Map<UUID, GameSessionEntity> entities) {
        return entities.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toDomain(e.getValue())));
    }

    /**
     * Преобразует объект игрового поля в сущность БД.
     *
     * @param domainMap доменная модель игрового поля.
     * @return сущность игрового поля для БД.
     */
    private static GameMapEntity toMapEntity(GameMap domainMap) {
        if (domainMap == null) return null;

        int size = domainMap.getSize();
        int[][] source = domainMap.getMap();
        int[][] target = new int[size][size];

        for (int i = 0; i < size; i++) {
            target[i] = source[i].clone();
        }

        return new GameMapEntity(target, size);
    }

    /**
     * Конвертирует статус игры из домена в сущность БД.
     *
     * @param domainStatus статус из бизнес-логики.
     * @return соответствующая сущность статуса для БД.
     */
    private static GameStatusEntity toStatusEntity(GameStatus domainStatus) {
        if (domainStatus == null) return null;
        return GameStatusEntity.valueOf(domainStatus.name());
    }

    /**
     * Преобразует сущность игрового поля обратно в доменную модель.
     *
     * @param entityMap сущность поля из БД.
     * @return доменная модель поля.
     */
    private static GameMap toMap(GameMapEntity entityMap) {
        if (entityMap == null) return null;

        int size = entityMap.getSize();
        int[][] source = entityMap.getMap();
        int[][] data = new int[size][size];

        for (int i = 0; i < size; i++) {
            data[i] = source[i].clone();
        }

        return new GameMap(data, size);
    }

    /**
     * Конвертирует статус игры из БД в доменную модель.
     *
     * @param entityStatus статус из БД.
     * @return доменный статус.
     */
    private static GameStatus toStatus(GameStatusEntity entityStatus) {
        if (entityStatus == null) return null;
        return GameStatus.valueOf(entityStatus.name());
    }
}