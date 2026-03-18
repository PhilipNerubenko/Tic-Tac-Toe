package org.example.datasource.storage;

import org.example.datasource.model.GameMapEntity;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.model.GameStatusEntity;
import org.example.datasource.repository.JpaGameRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class JpaGameRepositoryTest {
    private JpaGameRepository jpaGameRepository;

    JpaGameRepositoryTest(JpaGameRepository jpaGameRepository) {
        this.jpaGameRepository = jpaGameRepository;
    }

    @Test
    void save_ShouldSaveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        jpaGameRepository.save(entity);

        Assertions.assertEquals(entity, jpaGameRepository.findById(id));
    }

    @Test
    void removeById_ShouldRemoveEntity() {
        UUID id = UUID.randomUUID();
        GameSessionEntity entity = new GameSessionEntity(id, new GameMapEntity(3), GameStatusEntity.PLAYING);

        jpaGameRepository.save(entity);

        Assertions.assertEquals(entity, jpaGameRepository.findById(id));

        jpaGameRepository.deleteById(id);

        Assertions.assertNull(jpaGameRepository.findById(id));
    }
}