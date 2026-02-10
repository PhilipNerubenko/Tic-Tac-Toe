package org.example.domain.repository;

import org.example.domain.model.GameSession;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для управления состоянием игровых сессий.
 * <p>
 * Определяет контракт для сохранения, поиска и удаления игр.
 * Доменный слой использует этот интерфейс, не зная о деталях реализации
 * (будь то хранение в памяти, SQL или NoSQL база данных).
 */
public interface GameRepository {

    /**
     * Сохраняет или обновляет состояние игровой сессии.
     *
     * @param gameSession объект игровой сессии для сохранения.
     */
    void save(GameSession gameSession);

    /**
     * Выполняет поиск игровой сессии по её уникальному идентификатору.
     *
     * @param id уникальный идентификатор сессии (UUID).
     * @return {@link Optional}, содержащий найденную сессию,
     * или пустой {@link Optional}, если сессия не найдена.
     */
    Optional<GameSession> findById(UUID id);

    /**
     * Возвращает все существующие игровые сессии.
     *
     * @return карта всех сессий, где ключ — UUID, а значение — объект сессии.
     */
    Map<UUID, GameSession> getAll();

    /**
     * Удаляет игровую сессию из системы.
     *
     * @param id уникальный идентификатор сессии для удаления.
     */
    void removeById(UUID id);
}