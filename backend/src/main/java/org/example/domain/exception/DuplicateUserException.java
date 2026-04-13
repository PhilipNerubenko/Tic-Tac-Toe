package org.example.domain.exception;

/**
 * Исключение, выбрасываемое при попытке зарегистрировать пользователя
 * с логином, который уже занят.
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
