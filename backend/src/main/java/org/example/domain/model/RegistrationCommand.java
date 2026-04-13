package org.example.domain.model;

/**
 * Доменная команда для регистрации пользователя.
 * <p>
 * Используется как входной параметр для {@link org.example.domain.service.AuthService}.
 * Позволяет доменному слою не зависеть от DTO веб-слоя.
 *
 * @param login    уникальное имя пользователя.
 * @param password пароль пользователя (в открытом виде, будет захеширован сервисом).
 */
public record RegistrationCommand(String login, String password) {
}
