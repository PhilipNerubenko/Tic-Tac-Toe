package org.example.domain.exception;

public class IntegrityViolationException extends GameDomainException {
    public IntegrityViolationException() {
        super("Нарушена целостность игрового поля или нарушена очередность хода.");
    }
}