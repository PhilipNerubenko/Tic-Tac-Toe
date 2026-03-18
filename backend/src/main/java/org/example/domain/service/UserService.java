package org.example.domain.service;

import org.example.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления пользователями системы.
 * <p>
 * Содержит методы для регистрации, аутентификации и поиска пользователей.
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return созданный пользователь.
     * @throws IllegalArgumentException если пользователь с таким логином уже существует.
     */
    User register(String login, String password);

    /**
     * Находит пользователя по логину.
     *
     * @param login уникальное имя пользователя.
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findByLogin(String login);

    /**
     * Находит пользователя по уникальному идентификатору.
     *
     * @param id UUID пользователя.
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findById(UUID id);

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param login уникальное имя пользователя.
     * @return {@code true}, если пользователь с таким логином существует.
     */
    boolean existsByLogin(String login);

    /**
     * Проверяет соответствие логина и пароля.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return {@code true}, если логин и пароль верны.
     */
    boolean validateCredentials(String login, String password);
}
