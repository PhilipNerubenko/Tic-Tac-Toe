package org.example.web.model;

/**
 * Модель запроса на регистрацию пользователя.
 * <p>
 * Содержит логин и пароль для создания новой учетной записи.
 *
 * @param login    уникальное имя пользователя.
 * @param password пароль пользователя.
 */
public record SignUpRequest(String login, String password) {
}
