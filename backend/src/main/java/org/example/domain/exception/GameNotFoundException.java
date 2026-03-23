package org.example.domain.exception;

import java.util.UUID;

public class GameNotFoundException extends GameDomainException {
    public GameNotFoundException(UUID id) {
        super("Игра с UUID " + id + " не найдена.");
    }
}