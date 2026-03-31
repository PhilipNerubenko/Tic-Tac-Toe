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
import java.util.Collections;
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
        assertEquals("This is a utility class and cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    void toDTO_ShouldReturnNull_WhenArgumentNull() {
        assertNull(GameMapper.toEntity(null));
    }

    @Test
    void toDomain_ShouldReturnNull_WhenArgumentNull() {
        assertNull(GameMapper.toDomain(null));
    }

    @Test
    void toDTO_ShouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        int[][] rawMap = {{1, 0}, {0, 2}};
        GameMap domainMap = new GameMap(rawMap, 2);
        GameSession session = new GameSession(id, domainMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null);

        GameSessionEntity dto = GameMapper.toEntity(session);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(2, dto.getGameMap().getSize());
        assertArrayEquals(rawMap[0], dto.getGameMap().getMap()[0]);
        assertEquals(GameStatusEntity.PLAYER_TURN, dto.getStatus());
    }

    @Test
    void toDomain_ShouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        int[][] rawMap = {{1, 2}, {0, 0}};
        GameMapEntity entityMap = new GameMapEntity(rawMap, 2);
        GameSessionEntity entity = new GameSessionEntity(id, entityMap, GameStatusEntity.PLAYER_TURN, playerX, playerO, playerX, null);

        GameSession session = GameMapper.toDomain(entity);

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(2, session.getGameMap().getSize());
        assertArrayEquals(rawMap[1], session.getGameMap().getMap()[1]);
        assertEquals(GameStatus.PLAYER_TURN, session.getStatus());
    }

    @Test
    void toDomainMap_ShouldMapCorrectly() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();

        GameMapEntity mapEntity = new GameMapEntity(new int[][]{{0}}, 1);
        GameSessionEntity entity1 = new GameSessionEntity(id1, mapEntity, GameStatusEntity.PLAYER_TURN, playerX, playerO, playerX, null);
        GameSessionEntity entity2 = new GameSessionEntity(id2, mapEntity, GameStatusEntity.VICTORY, playerX, playerO, playerX, playerX);

        Map<UUID, GameSessionEntity> entityMap = Map.of(
                id1, entity1,
                id2, entity2
        );

        Map<UUID, GameSession> domainMap = GameMapper.toDomainMap(entityMap);

        assertNotNull(domainMap);
        assertEquals(2, domainMap.size());

        assertNotNull(domainMap.get(id1));
        assertEquals(id1, domainMap.get(id1).getId());
        assertEquals(GameStatus.PLAYER_TURN, domainMap.get(id1).getStatus());

        assertNotNull(domainMap.get(id2));
        assertEquals(id2, domainMap.get(id2).getId());
        assertEquals(GameStatus.VICTORY, domainMap.get(id2).getStatus());
    }

    @Test
    void toDomainMap_ShouldReturnEmptyMap_WhenInputIsEmpty() {
        Map<UUID, GameSession> result = GameMapper.toDomainMap(Collections.emptyMap());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDomainMap_ShouldHandleNullInput() {
        assertThrows(NullPointerException.class, () -> GameMapper.toDomainMap(null));
    }
}