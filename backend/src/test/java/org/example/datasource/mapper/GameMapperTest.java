package org.example.datasource.mapper;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameMapperTest {

    @Test
    void privateConstructorTest() throws Exception {
        Constructor<GameMapper> constructor = GameMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    void toDomain_ShouldReturnNull_WhenEntityNull() {
        assertNull(GameMapper.toDomain(null));
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDomainNull() {
        assertNull(GameMapper.toEntity(null));
    }

    @Test
    void toDomain_ShouldConvertEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMapEntity mapEntity = new GameMapEntity(3);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        Instant createdAt = Instant.now();
        GameSessionEntity entity = new GameSessionEntity(id, mapEntity, GameStatusEntity.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        GameSession domain = GameMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(playerX, domain.getPlayerX());
        assertEquals(playerO, domain.getPlayerO());
        assertEquals(GameStatus.PLAYER_TURN, domain.getStatus());
        assertEquals(3, domain.getGameMap().getSize());
        assertEquals(lastActiveAt, domain.getLastActiveAt());
    }

    @Test
    void toEntity_ShouldConvertDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(3);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        Instant createdAt = Instant.now();
        GameSession domain = new GameSession(id, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        GameSessionEntity entity = GameMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals(playerX, entity.getPlayerX());
        assertEquals(playerO, entity.getPlayerO());
        assertEquals(GameStatusEntity.PLAYER_TURN, entity.getStatus());
        assertEquals(3, entity.getGameMap().getSize());
    }

    @Test
    void toDomainMap_ShouldConvertMapOfEntities() {
        UUID id = UUID.randomUUID();
        GameMapEntity mapEntity = new GameMapEntity(3);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        Instant createdAt = Instant.now();
        GameSessionEntity entity = new GameSessionEntity(id, mapEntity, GameStatusEntity.PLAYER_TURN, UUID.randomUUID(), null, null, null, lastActiveAt, createdAt);

        Map<UUID, GameSessionEntity> entities = new HashMap<>();
        entities.put(id, entity);

        Map<UUID, GameSession> result = GameMapper.toDomainMap(entities);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(id));
        assertNotNull(result.get(id));
    }

    @Test
    void toDomainMap_ShouldHandleEmptyMap() {
        Map<UUID, GameSessionEntity> entities = new HashMap<>();
        Map<UUID, GameSession> result = GameMapper.toDomainMap(entities);
        assertTrue(result.isEmpty());
    }
}
