package org.example.domain.service;

import org.example.domain.model.RegistrationCommand;

import java.util.UUID;

/**
 * Интерфейс сервиса авторизации.
 * <p>
 * Содержит методы для регистрации и аутентификации пользователей.
 */
public interface AuthService {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param command команда на регистрацию с логином и паролем.
     * @return {@code true}, если регистрация прошла успешно.
     * @throws IllegalArgumentException если пользователь с таким логином уже существует.
     */
    boolean signUp(RegistrationCommand command);

    /**
     * Аутентифицирует пользователя по логину и паролю.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return UUID аутентифицированного пользователя.
     * @throws IllegalArgumentException если логин или пароль неверны.
     */
    UUID signIn(String login, String password);
}
