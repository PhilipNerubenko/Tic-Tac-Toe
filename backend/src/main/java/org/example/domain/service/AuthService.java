package org.example.domain.service;

import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.RegistrationCommand;
import org.example.web.model.JwtRequest;
import org.example.web.model.JwtResponse;

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
     * Аутентифицирует пользователя и возвращает JWT-токены.
     *
     * @param request запрос с логином и паролем.
     * @return JWT-токены (access и refresh).
     * @throws IllegalArgumentException если логин или пароль неверны.
     */
    JwtResponse signIn(JwtRequest request);

    /**
     * Обновляет access-токен по refresh-токену.
     *
     * @param refreshToken токен обновления.
     * @return новые JWT-токены (access и refresh).
     * @throws IllegalArgumentException если refresh-токен невалиден.
     */
    JwtResponse refreshAccessToken(String refreshToken);

    /**
     * Обновляет refresh-токен по текущему refresh-токену.
     *
     * @param refreshToken текущий токен обновления.
     * @return новые JWT-токены (access и refresh).
     * @throws IllegalArgumentException если refresh-токен невалиден.
     */
    JwtResponse refreshRefreshToken(String refreshToken);

    /**
     * Получает объект аутентификации по access-токену.
     *
     * @param accessToken токен доступа.
     * @return объект JwtAuthentication.
     * @throws IllegalArgumentException если токен невалиден.
     */
    JwtAuthentication getAuthentication(String accessToken);
}
