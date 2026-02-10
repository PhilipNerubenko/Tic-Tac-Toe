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
        int[][] rawMap = {{1, 0}, {0, 2}};
        GameMap domainMap = new GameMap(rawMap, 2);
        GameSession session = new GameSession(id, domainMap, GameStatus.PLAYING);

        GameSessionDTO dto = GameMapperDTO.toDTO(session);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(2, dto.getGameMap().getSize());
        assertArrayEquals(rawMap[0], dto.getGameMap().getMap()[0]);
        assertEquals(GameStatusDTO.PLAYING, dto.getStatus());
    }

    @Test
    void toDomain_ShouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        int[][] rawMap = {{1, 2}, {0, 0}};
        GameMapDTO dtoMap = new GameMapDTO(rawMap, 2);
        GameSessionDTO dto = new GameSessionDTO(id, dtoMap, GameStatusDTO.PLAYING);

        GameSession session = GameMapperDTO.toDomain(dto);

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(2, session.getGameMap().getSize());
        assertArrayEquals(rawMap[1], session.getGameMap().getMap()[1]);
        assertEquals(GameStatus.PLAYING, session.getStatus());
    }
}