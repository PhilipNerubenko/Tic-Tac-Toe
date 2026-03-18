package org.example.datasource.repository;

import org.example.datasource.model.GameSessionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface JpaGameRepository extends CrudRepository<GameSessionEntity, UUID> {
}
