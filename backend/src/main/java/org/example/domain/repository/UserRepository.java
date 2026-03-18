package org.example.domain.repository;

import org.example.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для управления пользователями системы.
 * <p>
 * Определяет контракт для сохранения, поиска и удаления пользователей.
 * Доменный слой использует этот интерфейс, не зная о деталях реализации
 * (будь то хранение в памяти, SQL или NoSQL база данных).
 */
public interface UserRepository {

    /**
     * Сохраняет или обновляет пользователя.
     *
     * @param user объект пользователя для сохранения.
     */
    void save(User user);

    /**
     * Выполняет поиск пользователя по уникальному идентификатору.
     *
     * @param id уникальный идентификатор пользователя (UUID).
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findById(UUID id);

    /**
     * Выполняет поиск пользователя по логину.
     *
     * @param login уникальное имя пользователя.
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findByLogin(String login);

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param login уникальное имя пользователя.
     * @return {@code true}, если пользователь с таким логином существует.
     */
    boolean existsByLogin(String login);
}
