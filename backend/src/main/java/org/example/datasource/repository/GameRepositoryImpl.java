package org.example.datasource.repository;

import org.example.datasource.mapper.GameMapper;
import org.example.datasource.model.GameSessionEntity;
import org.example.datasource.storage.GameStorage;
import org.example.domain.model.GameSession;
import org.example.domain.repository.GameRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация интерфейса репозитория для управления игровыми сессиями.
 * <p>
 * Данный класс отвечает за координацию между хранилищем {@link GameStorage}
 * и механизмом преобразования данных {@link GameMapper}.
 */
public class GameRepositoryImpl implements GameRepository {

    /** Хранилище данных (например, в оперативной памяти или БД) */
    private final GameStorage gameStorage;

    /**
     * Создает экземпляр репозитория.
     * @param gameStorage реализация хранилища данных.
     */
    public GameRepositoryImpl(GameStorage gameStorage) {
        this.gameStorage = gameStorage;
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
        gameStorage.save(gameSessionEntity);
    }

    /**
     * Находит игровую сессию по её уникальному идентификатору.
     *
     * @param id UUID сессии.
     * @return {@link Optional}, содержащий доменную модель игры,
     * или пустой Optional, если игра не найдена.
     */
    @Override
    public Optional<GameSession> findById(UUID id) {
        GameSessionEntity gameSessionEntity = gameStorage.findById(id);

        return Optional.ofNullable(gameSessionEntity)
                .map(GameMapper::toDomain);
    }

    /**
     * Удаляет игровую сессию из хранилища.
     *
     * @param id UUID сессии для удаления.
     */
    @Override
    public void removeById(UUID id) {
        gameStorage.removeById(id);
    }

    /**
     * Возвращает все активные и завершенные игровые сессии.
     *
     * @return карта всех игровых сессий в формате доменных моделей.
     */
    @Override
    public Map<UUID, GameSession> getAll() {
        return GameMapper.toDomainMap(gameStorage.getAll());
    }
}