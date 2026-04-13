package org.example.domain.exception;

/**
 * Исключение, выбрасываемое при попытке сделать некорректный ход
 * (например, выход за границы поля или неверные координаты).
 */
public class InvalidMoveException extends GameDomainException {
    public InvalidMoveException(String message) {
        super(message);
    }
}
