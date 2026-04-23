package org.example.web.mapper;

import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.example.web.model.GameMapDTO;
import org.example.web.model.GameSessionDTO;
import org.example.web.model.GameStatusDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameMapperDTOTest {

    @Test
    void privateConstructorTest() throws Exception {
        Constructor<GameMapperDTO> constructor = GameMapperDTO.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("This is a utility class and cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    void toDTO_ShouldReturnNull_WhenArgumentNull() {
        assertNull(GameMapperDTO.toDTO(null));
    }

    @Test
    void toDomain_ShouldReturnNull_WhenArgumentNull() {
        assertNull(GameMapperDTO.toDomain(null));
    }

    @Test
    void toDTO_ShouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        int[][] rawMap = {{1, 0}, {0, 2}};
        GameMap domainMap = new GameMap(rawMap, 2);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();
        GameSession session = new GameSession(id, domainMap, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        GameSessionDTO dto = GameMapperDTO.toDTO(session);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(2, dto.getGameMap().getSize());
        assertArrayEquals(rawMap[0], dto.getGameMap().getMap()[0]);
        assertEquals(GameStatusDTO.PLAYER_TURN, dto.getStatus());
    }

    @Test
    void toDomain_ShouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        int[][] rawMap = {{1, 2}, {0, 0}};
        GameMapDTO dtoMap = new GameMapDTO(rawMap, 2);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();
        GameSessionDTO dto = new GameSessionDTO(id, dtoMap, GameStatusDTO.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        GameSession session = GameMapperDTO.toDomain(dto);

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(2, session.getGameMap().getSize());
        assertArrayEquals(rawMap[1], session.getGameMap().getMap()[1]);
        assertEquals(GameStatus.PLAYER_TURN, session.getStatus());
    }
}