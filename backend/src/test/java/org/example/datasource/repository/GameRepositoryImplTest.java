package org.example.datasource.repository;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.datasource.storage.GameStorage;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRepositoryImplTest {

    @Mock
    private GameStorage gameStorage;

    @InjectMocks
    private GameRepositoryImpl repository;

    @Test
    void findById_ShouldReturnEmptyOptional_WhenStorageReturnsNull() {
        UUID id = UUID.randomUUID();
        when(gameStorage.findById(id)).thenReturn(null);

        Optional<GameSession> result = repository.findById(id);

        assertTrue(result.isEmpty());
        verify(gameStorage).findById(id);
    }

    @Test
    void save_ShouldCallStorageSave() {
        UUID id = UUID.randomUUID();
        GameSession session = new GameSession(id, null, GameStatus.PLAYING);

        repository.save(session);

        verify(gameStorage).save(argThat(entity -> entity.getId().equals(id)));
    }

    @Test
    void removeById_ShouldCallStorageRemove() {
        UUID id = UUID.randomUUID();

        repository.removeById(id);

        verify(gameStorage, times(1)).removeById(id);
    }

    @Test
    void getAll_ShouldReturnMappedMap() {
        UUID id = UUID.randomUUID();
        GameMapEntity mapEntity = new GameMapEntity(new int[][]{{0}}, 1);
        GameSessionEntity entity = new GameSessionEntity(id, mapEntity, GameStatusEntity.PLAYING);

        Map<UUID, GameSessionEntity> storageMap = Map.of(id, entity);
        when(gameStorage.getAll()).thenReturn(storageMap);

        Map<UUID, GameSession> result = repository.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(id));
        assertEquals(GameStatus.PLAYING, result.get(id).getStatus());

        verify(gameStorage).getAll();
    }

    @Test
    void getAll_ShouldReturnEmptyMap_WhenStorageIsEmpty() {
        when(gameStorage.getAll()).thenReturn(Map.of());

        Map<UUID, GameSession> result = repository.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}