package org.example.web.controller;

import org.example.domain.exception.GameDomainException;
import org.example.domain.exception.GameNotFoundException;
import org.example.domain.exception.IntegrityViolationException;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.exception.NotGameParticipantException;
import org.example.domain.exception.NotYourTurnException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений.
 * Перехватывает доменные ошибки и превращает их в понятные HTTP ответы.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(GameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IntegrityViolationException.class)
    public ResponseEntity<Object> handleViolation(IntegrityViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotYourTurnException.class)
    public ResponseEntity<Object> handleNotYourTurn(NotYourTurnException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(GameDomainException.class)
    public ResponseEntity<Object> handleGameDomain(GameDomainException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotGameParticipantException.class)
    public ResponseEntity<Object> handleNotGameParticipant(NotGameParticipantException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<Object> handleInvalidMove(InvalidMoveException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}