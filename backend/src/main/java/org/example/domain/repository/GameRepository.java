package org.example.domain.repository;

import org.example.domain.model.GameSession;

import java.util.List;
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
     * Используется для операций только на чтение (без блокировки).
     *
     * @param id уникальный идентификатор сессии (UUID).
     * @return {@link Optional}, содержащий найденную сессию,
     * или пустой {@link Optional}, если сессия не найдена.
     */
    Optional<GameSession> findById(UUID id);

    /**
     * Выполняет поиск игровой сессии с пессимистичной блокировкой для записи.
     * Используется перед операциями изменения (executeTurn, joinPlayer).
     *
     * @param id уникальный идентификатор сессии (UUID).
     * @return {@link Optional}, содержащий найденную сессию,
     * или пустой {@link Optional}, если сессия не найдена.
     */
    Optional<GameSession> findByIdForUpdate(UUID id);

    /**
     * Возвращает все существующие игровые сессии.
     *
     * @return карта всех сессий, где ключ — UUID, а значение — объект сессии.
     */
    Map<UUID, GameSession> findAll();

    /**
     * Возвращает все завершённые игры по UUID игрока.
     *
     * @param userUuid UUID игрока, историю которого нужно найти.
     * @return Список завершённых игровых сессий.
     */
    List<GameSession> findAllFinishedByPlayerUuid(UUID userUuid);

    /**
     * Удаляет игровую сессию из системы.
     *
     * @param id уникальный идентификатор сессии для удаления.
     */
    void deleteById(UUID id);
}