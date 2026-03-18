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
    private JpaGameRepository jpaGameRepository;

    @InjectMocks
    private GameRepositoryImpl repository;

    @Test
    void findById_ShouldReturnEmptyOptional_WhenStorageReturnsNull() {
        UUID id = UUID.randomUUID();
        when(jpaGameRepository.findById(id)).thenReturn(null);

        Optional<GameSession> result = repository.findById(id);

        assertTrue(result.isEmpty());
        verify(jpaGameRepository).findById(id);
    }

    @Test
    void save_ShouldCallStorageSave() {
        UUID id = UUID.randomUUID();
        GameSession session = new GameSession(id, null, GameStatus.PLAYING);

        repository.save(session);

        verify(jpaGameRepository).save(argThat(entity -> entity.getId().equals(id)));
    }

    @Test
    void removeById_ShouldCallStorageDelete() {
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(jpaGameRepository, times(1)).deleteById(id);
    }

    @Test
    void findAll_ShouldReturnMappedMap() {
        UUID id = UUID.randomUUID();
        GameMapEntity mapEntity = new GameMapEntity(new int[][]{{0}}, 1);
        GameSessionEntity entity = new GameSessionEntity(id, mapEntity, GameStatusEntity.PLAYING);

        Map<UUID, GameSessionEntity> storageMap = Map.of(id, entity);
        when(jpaGameRepository.getAll()).thenReturn(storageMap);

        Map<UUID, GameSession> result = repository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(id));
        assertEquals(GameStatus.PLAYING, result.get(id).getStatus());

        verify(jpaGameRepository).getAll();
    }

    @Test
    void findAll_ShouldReturnEmptyMap_WhenStorageIsEmpty() {
        when(jpaGameRepository.getAll()).thenReturn(Map.of());

        Map<UUID, GameSession> result = repository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}