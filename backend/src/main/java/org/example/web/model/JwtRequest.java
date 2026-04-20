package org.example.web.model;

/**
 * Модель запроса для аутентификации и получения JWT-токена.
 * <p>
 * Содержит логин и пароль пользователя.
 *
 * @param login    имя пользователя.
 * @param password пароль пользователя.
 */
public record JwtRequest(String login, String password) {
}
