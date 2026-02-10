package org.example.datasource.storage;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameStorageTest {
    private GameStorage gameStorage;

    @BeforeEach
    void setUp() {
        gameStorage = new GameStorage();
    }

    @Test
    void save_ShouldSaveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        gameStorage.save(entity);

        Assertions.assertEquals(entity, gameStorage.findById(id));
    }

    @Test
    void removeById_ShouldRemoveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        gameStorage.save(entity);

        Assertions.assertEquals(entity, gameStorage.findById(id));

        gameStorage.removeById(id);

        Assertions.assertNull(gameStorage.findById(id));
    }

    @Test
    void save_ShouldDoNothing_WhenEntityIsNull() {

        gameStorage.save(null);

        Assertions.assertTrue(gameStorage.getAll().isEmpty());
    }

    @Test
    void save_ShouldNotSave_WhenEntityIdIsNull() {
        GameSessionEntity entityWithoutId = new GameSessionEntity(null, new GameMapEntity(3), GameStatusEntity.PLAYING);

        gameStorage.save(entityWithoutId);

        Assertions.assertEquals(0, gameStorage.getAll().size());
    }

    @Test
    void findById() {
    }

    @Test
    void removeById() {
    }

    @Test
    void getAll() {
    }
}