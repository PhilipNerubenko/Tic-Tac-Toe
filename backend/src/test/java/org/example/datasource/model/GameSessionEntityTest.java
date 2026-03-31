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
        assertEquals(GameStatusEntity.WAITING_FOR_PLAYERS, entity.getStatus(), "Initial status should be WAITING_FOR_PLAYERS");
    }

    @Test
    void setStatus_ShouldUpdateStatus() {
        GameMapEntity map = new GameMapEntity(3);
        GameSessionEntity entity = new GameSessionEntity(map);

        entity.setStatus(GameStatusEntity.VICTORY);

        assertEquals(GameStatusEntity.VICTORY, entity.getStatus(), "Status should be updated to VICTORY");
    }

    @Test
    void constructorForRestore_ShouldPreserveAllFields() {
        UUID fixedId = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMapEntity map = new GameMapEntity(3);
        GameStatusEntity status = GameStatusEntity.DRAW;

        GameSessionEntity entity = new GameSessionEntity(fixedId, map, status, playerX, playerO, playerX, null);

        assertEquals(fixedId, entity.getId(), "Should preserve the provided UUID");
        assertEquals(map, entity.getGameMap(), "Should preserve the provided map");
        assertEquals(status, entity.getStatus(), "Should preserve the provided status");
    }
}