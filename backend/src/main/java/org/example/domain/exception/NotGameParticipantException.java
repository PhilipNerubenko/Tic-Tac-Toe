package org.example.domain.exception;

/**
 * Исключение, выбрасываемое когда пользователь пытается получить доступ к игре,
 * участником которой он не является.
 */
public class NotGameParticipantException extends GameDomainException {
    public NotGameParticipantException(String message) {
        super(message);
    }
}
