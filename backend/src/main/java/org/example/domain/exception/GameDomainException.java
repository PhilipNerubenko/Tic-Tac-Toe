package org.example.domain.exception;

/**
 * Базовое исключение для всех бизнес-ошибок домена игры.
 */
public class GameDomainException extends RuntimeException {
    public GameDomainException(String message) {
        super(message);
    }
}