package org.example.datasource.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionEntityTest {

    @Test
    void constructorWithMap_ShouldInitializeNewGame() {
        GameMapEntity map = new GameMapEntity(3);

        GameSessionEntity entity = new GameSessionEntity(map);

        assertNotNull(entity.getId(), "ID should be generated automatically");
        assertEquals(map, entity.getGameMap(), "Map should match the one passed to constructor");
        assertEquals(GameStatusEntity.PLAYING, entity.getStatus(), "Initial status should be PLAYING");
    }

    @Test
    void setStatus_ShouldUpdateStatus() {
        GameMapEntity map = new GameMapEntity(3);
        GameSessionEntity entity = new GameSessionEntity(map);

        entity.setStatus(GameStatusEntity.CROSS_WIN);

        assertEquals(GameStatusEntity.CROSS_WIN, entity.getStatus(), "Status should be updated to CROSS_WIN");
    }

    @Test
    void constructorForRestore_ShouldPreserveAllFields() {
        UUID fixedId = UUID.randomUUID();
        GameMapEntity map = new GameMapEntity(3);
        GameStatusEntity status = GameStatusEntity.DRAW;

        GameSessionEntity entity = new GameSessionEntity(fixedId, map, status);

        assertEquals(fixedId, entity.getId(), "Should preserve the provided UUID");
        assertEquals(map, entity.getGameMap(), "Should preserve the provided map");
        assertEquals(status, entity.getStatus(), "Should preserve the provided status");
    }
}