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
     * Конструктор по умолчанию.
     */
    private GameMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Преобразует сущность БД в объект доменной области.
     * Используется при извлечении данных из репозитория.
     *
     * @param gameSessionEntity сущность, полученная из источника данных.
     * @return объект {@link GameSession} или {@code null}, если входные данные отсутствуют.
     */
    public static GameSession toDomain(GameSessionEntity gameSessionEntity) {
        if (gameSessionEntity == null) return null;

        return new GameSession(
                gameSessionEntity.getId(),
                toMap(gameSessionEntity.getGameMap()),
                toStatus(gameSessionEntity.getStatus())
        );
    }

    /**
     * Преобразует доменную модель сессии в сущность для сохранения в БД.
     *
     * @param gameSession объект доменной области.
     * @return сущность {@link GameSessionEntity} или {@code null}, если объект пуст.
     */
    public static GameSessionEntity toEntity(GameSession gameSession) {
        if (gameSession == null) return null;

        return new GameSessionEntity(
                gameSession.getId(),
                toMapEntity(gameSession.getGameMap()),
                toStatusEntity(gameSession.getStatus())
        );
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
     * <p>
     * Выполняет <b>глубокое копирование</b> массива данных для предотвращения
     * побочных эффектов при изменении данных в разных слоях.
     *
     * @param domainMap доменная модель игрового поля.
     * @return сущность игрового поля для БД.
     */
    private static GameMapEntity toMapEntity(GameMap domainMap) {
        if (domainMap == null) return null;

        int size = domainMap.getSize();
        int[][] source = domainMap.getMap();
        int[][] target = new int[size][size];

        // Клонируем каждую строку массива отдельно
        for (int i = 0; i < size; i++) {
            target[i] = source[i].clone();
        }

        return new GameMapEntity(target, size);
    }

    /**
     * Конвертирует статус игры из домена в сущность БД.
     * Основано на соответствии строковых имен констант Enum.
     *
     * @param domainStatus статус из бизнес-логики.
     * @return соответствующая сущность статуса для БД.
     */
    private static GameStatusEntity toStatusEntity(GameStatus domainStatus) {
        return GameStatusEntity.valueOf(domainStatus.name());
    }

    /**
     * Преобразует сущность игрового поля обратно в доменную модель.
     * Также выполняет глубокое копирование данных массива.
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
     * @param domainStatusEntity статус из БД.
     * @return доменный статус.
     */
    private static GameStatus toStatus(GameStatusEntity domainStatusEntity) {
        return GameStatus.valueOf(domainStatusEntity.name());
    }
}