package org.example.domain.exception;

/**
 * Исключение, выбрасываемое когда игрок пытается сделать ход, но сейчас не его очередь.
 */
public class NotYourTurnException extends GameDomainException {
    public NotYourTurnException(String message) {
        super(message);
    }
}
