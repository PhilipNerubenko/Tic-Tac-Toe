package org.example.web.model;

/**
 * Модель ответа для передачи JWT-токенов.
 * <p>
 * Содержит тип токена, access-токен и refresh-токен.
 *
 * @param type        тип токена (например, "Bearer").
 * @param accessToken токен доступа.
 * @param refreshToken токен обновления.
 */
public record JwtResponse(String type, String accessToken, String refreshToken) {
}
