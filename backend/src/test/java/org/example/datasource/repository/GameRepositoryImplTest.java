package org.example.datasource.repository;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.GameStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
        when(jpaGameRepository.findByIdReadOnly(id)).thenReturn(Optional.empty());

        Optional<GameSession> result = repository.findById(id);

        assertTrue(result.isEmpty());
        verify(jpaGameRepository).findByIdReadOnly(id);
    }

    @Test
    void save_ShouldCallStorageSave() {
        UUID id = UUID.randomUUID();
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMap map = new GameMap(3);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();
        GameSession session = new GameSession(id, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

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
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameMapEntity mapEntity = new GameMapEntity(new int[][]{{0}}, 1);
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();

        GameSessionEntity entity = new GameSessionEntity(id, mapEntity, GameStatusEntity.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        when(jpaGameRepository.findAll()).thenReturn(java.util.List.of(entity));

        Map<UUID, GameSession> result = repository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(id));
        assertEquals(GameStatus.PLAYER_TURN, result.get(id).getStatus());

        verify(jpaGameRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyMap_WhenStorageIsEmpty() {
        when(jpaGameRepository.findAll()).thenReturn(java.util.List.of());

        Map<UUID, GameSession> result = repository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}