package org.example.datasource.storage;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.datasource.repository.JpaGameRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaGameRepositoryTest {
    @Mock
    private JpaGameRepository jpaGameRepository;

    @Test
    void save_ShouldSaveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        when(jpaGameRepository.save(entity)).thenReturn(entity);
        when(jpaGameRepository.findById(id)).thenReturn(Optional.of(entity));

        jpaGameRepository.save(entity);

        Assertions.assertEquals(entity, jpaGameRepository.findById(id).orElse(null));
    }

    @Test
    void removeById_ShouldRemoveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        when(jpaGameRepository.save(entity)).thenReturn(entity);
        when(jpaGameRepository.findById(id)).thenReturn(Optional.of(entity));

        jpaGameRepository.save(entity);

        Assertions.assertEquals(entity, jpaGameRepository.findById(id).orElse(null));

        jpaGameRepository.deleteById(id);

        when(jpaGameRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertTrue(jpaGameRepository.findById(id).isEmpty());
    }
}