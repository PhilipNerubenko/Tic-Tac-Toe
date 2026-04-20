package org.example.web.model;

/**
 * Модель запроса для обновления JWT-токена.
 * <p>
 * Содержит refresh-токен.
 *
 * @param refreshToken токен обновления.
 */
public record RefreshJwtRequest(String refreshToken) {
}
