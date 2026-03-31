package org.example.datasource.repository;

import jakarta.persistence.LockModeType;
import org.example.datasource.model.GameSessionEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaGameRepository extends CrudRepository<GameSessionEntity, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GameSessionEntity g where g.id = :id")
    Optional<GameSessionEntity> findById(@Param("id") UUID id);

    @Query("select g from GameSessionEntity g where g.id = :id")
    Optional<GameSessionEntity> findByIdReadOnly(@Param("id") UUID id);
}