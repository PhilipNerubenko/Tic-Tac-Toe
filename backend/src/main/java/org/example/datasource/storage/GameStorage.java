package org.example.datasource.storage;

import org.example.datasource.model.GameSessionEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * Вспомогательное хранилище игровых сессий в оперативной памяти (In-Memory storage).
 * <p>
 * Класс обеспечивает потокобезопасный доступ к данным, что позволяет
 * нескольким игрокам взаимодействовать с сервером одновременно без риска потери данных.
 */
public class GameStorage {

    /**
     * Конструктор по умолчанию.
     * Инициализирует хранилище сессий.
     */
    public GameStorage() {
    }

    /**
     * Хранилище сессий.
     * Используется {@link ConcurrentHashMap} для обеспечения thread-safety при параллельных запросах.
     */
    private final Map<UUID, GameSessionEntity> allGames = new ConcurrentHashMap<>();

    /**
     * Сохраняет или обновляет игровую сессию.
     * Проверяет входной объект на {@code null}, чтобы избежать повреждения данных.
     *
     * @param game сущность игровой сессии.
     */
    public void save(GameSessionEntity game) {
        if (game != null && game.getId() != null) {
            allGames.put(game.getId(), game);
        }
    }

    /**
     * Возвращает сессию по её идентификатору.
     *
     * @param id уникальный идентификатор сессии.
     * @return {@link GameSessionEntity} или {@code null}, если игра с таким ID не найдена.
     */
    public GameSessionEntity findById(UUID id) {
        return allGames.get(id);
    }

    /**
     * Удаляет данные об игре из оперативной памяти.
     *
     * @param id идентификатор сессии, которую нужно удалить.
     */
    public void removeById(UUID id) {
        allGames.remove(id);
    }

    /**
     * Предоставляет доступ ко всем хранящимся играм.
     * <p>
     * Возвращает <b>неизменяемое представление</b> (unmodifiable view) карты,
     * чтобы предотвратить случайное изменение хранилища в обход методов {@code save} или {@code remove}.
     *
     * @return защищенная от записи карта всех игровых сессий.
     */
    public Map<UUID, GameSessionEntity> getAll() {
        return Collections.unmodifiableMap(allGames);
    }
}