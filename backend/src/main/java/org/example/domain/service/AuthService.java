package org.example.domain.service;

import org.example.web.model.SignUpRequest;

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
     * @param request запрос на регистрацию с логином и паролем.
     * @return {@code true}, если регистрация прошла успешно.
     * @throws IllegalArgumentException если пользователь с таким логином уже существует.
     */
    boolean signUp(SignUpRequest request);

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
