package org.example.datasource.repository;

import jakarta.persistence.LockModeType;
import org.example.datasource.model.GameSessionEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaGameRepository extends CrudRepository<GameSessionEntity, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GameSessionEntity g where g.id = :id")
    Optional<GameSessionEntity> findById(@Param("id") UUID id);

    @Query("select g from GameSessionEntity g where g.id = :id")
    Optional<GameSessionEntity> findByIdReadOnly(@Param("id") UUID id);

    @Query("SELECT g FROM GameSessionEntity g WHERE (g.playerX = :uuid OR g.playerO = :uuid) " +
            "AND g.status IN (org.example.datasource.model.GameStatusEntity.VICTORY, " +
            "org.example.datasource.model.GameStatusEntity.DRAW, " +
            "org.example.datasource.model.GameStatusEntity.OPPONENT_LEFT)")
    List<GameSessionEntity> findAllFinishedByPlayerUuid(@Param("uuid") UUID uuid);

    @Query(value = """
    SELECT u.id as userId, u.login as login,
           COUNT(g.id) as totalGames,
           COUNT(CASE WHEN g.winner_id = u.id THEN 1 END) as totalWins,
            COUNT(CASE WHEN g.winner_id = u.id THEN 1 END)::double precision / COUNT(g.id) as winRate
    FROM users u
    JOIN game_sessions g ON (g.player_x_id = u.id OR g.player_o_id = u.id)
    WHERE g.status IN ('VICTORY', 'DRAW', 'OPPONENT_LEFT')
    GROUP BY u.id, u.login
    HAVING COUNT(g.id) >= :minGames
    ORDER BY winRate DESC, totalWins DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopLeadersNative(@Param("limit") int limit, @Param("minGames") int minGames);
}