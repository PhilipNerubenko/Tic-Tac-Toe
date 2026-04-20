package org.example.datasource.repository;

import org.example.datasource.mapper.GameMapper;
import org.example.datasource.model.GameSessionEntity;
import org.example.domain.model.GameSession;
import org.example.domain.repository.GameRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация интерфейса репозитория для управления игровыми сессиями.
 * <p>
 * Данный класс отвечает за координацию между хранилищем {@link JpaGameRepository}
 * и механизмом преобразования данных {@link GameMapper}.
 */
public class GameRepositoryImpl implements GameRepository {

    /** Хранилище данных (например, в оперативной памяти или БД) */
    private final JpaGameRepository jpaGameRepository;

    /**
     * Создает экземпляр репозитория.
     * @param jpaGameRepository реализация хранилища данных.
     */
    public GameRepositoryImpl(JpaGameRepository jpaGameRepository) {
        this.jpaGameRepository = jpaGameRepository;
    }

    /**
     * Сохраняет состояние игры.
     * Преобразует доменную модель в сущность БД перед сохранением.
     *
     * @param gameSession доменная модель игровой сессии.
     */
    @Override
    public void save(GameSession gameSession) {
        GameSessionEntity gameSessionEntity = GameMapper.toEntity(gameSession);
        jpaGameRepository.save(gameSessionEntity);
    }

    /**
     * Находит игровую сессию по её уникальному идентификатору.
     * Используется для операций только на чтение (без блокировки).
     *
     * @param id UUID сессии.
     * @return {@link Optional}, содержащий доменную модель игры,
     * или пустой Optional, если игра не найдена.
     */
    @Override
    @Transactional
    public Optional<GameSession> findById(UUID id) {
        return jpaGameRepository.findByIdReadOnly(id)
                .map(GameMapper::toDomain);
    }

    /**
     * Находит игровую сессию с пессимистичной блокировкой для записи.
     * Используется перед операциями изменения (executeTurn, joinPlayer).
     * <p>
     * ВАЖНО: этот метод должен вызываться внутри транзакции сервиса,
     * чтобы блокировка сохранялась до завершения операции сохранения.
     *
     * @param id UUID сессии.
     * @return {@link Optional}, содержащий доменную модель игры,
     * или пустой Optional, если игра не найдена.
     */
    @Override
    public Optional<GameSession> findByIdForUpdate(UUID id) {
        return jpaGameRepository.findById(id)
                .map(GameMapper::toDomain);
    }

    /**
     * Удаляет игровую сессию из хранилища.
     *
     * @param id UUID сессии для удаления.
     */
    @Override
    public void deleteById(UUID id) {
        jpaGameRepository.deleteById(id);
    }

    /**
     * Возвращает все активные и завершенные игровые сессии.
     *
     * @return карта всех игровых сессий в формате доменных моделей.
     */
    @Override
    public Map<UUID, GameSession> findAll() {
        Iterable<GameSessionEntity> entities = jpaGameRepository.findAll();

        return StreamSupport.stream(entities.spliterator(), false)
                .map(GameMapper::toDomain)
                .collect(Collectors.toMap(GameSession::getId, session -> session));
    }

    @Override
    public List<GameSession> findAllFinishedByPlayerUuid(UUID userUuid) {
        List<GameSessionEntity> entities = jpaGameRepository.findAllFinishedByPlayerUuid(userUuid);

        return entities.stream()
                .map(GameMapper::toDomain)
                .toList();
    }
}