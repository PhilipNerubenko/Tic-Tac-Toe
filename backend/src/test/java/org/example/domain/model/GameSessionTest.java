package org.example.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    void constructor_WithVsAi_ShouldSetPlayerOToAI() {
        GameMap map = new GameMap(3);
        UUID creatorId = UUID.randomUUID();
        GameSession session = new GameSession(map, creatorId, true);

        assertThat(session.getPlayerO()).isEqualTo(GameSession.AI_PLAYER_ID);
        assertThat(session.getPlayerX()).isEqualTo(creatorId);
        assertThat(session.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
        assertThat(session.getCurrentPlayer()).isEqualTo(creatorId);
    }

    @Test
    void constructor_WithoutVsAi_ShouldSetWaitingForPlayers() {
        GameMap map = new GameMap(3);
        UUID creatorId = UUID.randomUUID();
        GameSession session = new GameSession(map, creatorId, false);

        assertThat(session.getPlayerO()).isNull();
        assertThat(session.getStatus()).isEqualTo(GameStatus.WAITING_FOR_PLAYERS);
    }

    @Test
    void joinOpponent_ShouldSetPlayerOAndStatus() {
        GameMap map = new GameMap(3);
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        GameSession session = new GameSession(map, creatorId, false);

        session.joinOpponent(opponentId);

        assertThat(session.getPlayerO()).isEqualTo(opponentId);
        assertThat(session.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
        assertThat(session.getCurrentPlayer()).isEqualTo(creatorId);
    }

    @Test
    void joinOpponent_ShouldThrowException_WhenGameAlreadyStarted() {
        GameMap map = new GameMap(3);
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        GameSession session = new GameSession(map, creatorId, true);

        assertThrows(IllegalStateException.class, () -> session.joinOpponent(opponentId));
    }

    @Test
    void joinOpponent_ShouldThrowException_WhenSamePlayer() {
        GameMap map = new GameMap(3);
        UUID creatorId = UUID.randomUUID();
        GameSession session = new GameSession(map, creatorId, false);

        assertThrows(IllegalArgumentException.class, () -> session.joinOpponent(creatorId));
    }

    @Test
    void switchTurn_ShouldSwitchBetweenPlayers() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, false);
        session.joinOpponent(playerO);

        assertThat(session.getCurrentPlayer()).isEqualTo(playerX);

        session.switchTurn();
        assertThat(session.getCurrentPlayer()).isEqualTo(playerO);

        session.switchTurn();
        assertThat(session.getCurrentPlayer()).isEqualTo(playerX);
    }

    @Test
    void switchTurn_ShouldDoNothing_WhenNotPlayerTurn() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);
        session.setStatus(GameStatus.VICTORY);

        UUID currentPlayerBefore = session.getCurrentPlayer();
        session.switchTurn();

        assertThat(session.getCurrentPlayer()).isEqualTo(currentPlayerBefore);
    }

    @Test
    void isGameOver_ShouldReturnTrue_WhenVictory() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);
        session.setStatus(GameStatus.VICTORY);

        assertThat(session.isGameOver()).isTrue();
    }

    @Test
    void isGameOver_ShouldReturnTrue_WhenDraw() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);
        session.setStatus(GameStatus.DRAW);

        assertThat(session.isGameOver()).isTrue();
    }

    @Test
    void isGameOver_ShouldReturnFalse_WhenPlayerTurn() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        assertThat(session.isGameOver()).isFalse();
    }

    @Test
    void isWaitingForMoveFromPlayer_ShouldReturnTrue_WhenCorrectPlayer() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        assertThat(session.isWaitingForMoveFromPlayer(playerX)).isTrue();
    }

    @Test
    void isWaitingForMoveFromPlayer_ShouldReturnFalse_WhenWrongPlayer() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        UUID otherPlayer = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        assertThat(session.isWaitingForMoveFromPlayer(otherPlayer)).isFalse();
    }

    @Test
    void isPlayer_ShouldReturnTrue_WhenPlayerX() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        assertThat(session.isPlayer(playerX)).isTrue();
    }

    @Test
    void isPlayer_ShouldReturnTrue_WhenPlayerO() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        UUID playerO = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        java.time.Instant lastActiveAt = java.time.Instant.now();
        LocalDateTime createdAt = LocalDateTime.now();
        GameSession session = new GameSession(sessionId, map, GameStatus.PLAYER_TURN, playerX, playerO, playerX, null, lastActiveAt, createdAt);

        assertThat(session.isPlayer(playerO)).isTrue();
    }

    @Test
    void isPlayer_ShouldReturnFalse_WhenNotPlayer() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        UUID otherPlayer = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        assertThat(session.isPlayer(otherPlayer)).isFalse();
    }

    @Test
    void updateLastActiveAt_ShouldUpdateTime() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        java.time.Instant before = session.getLastActiveAt();
        
        // Небольшая задержка для гарантии различия времени
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        session.updateLastActiveAt();
        java.time.Instant after = session.getLastActiveAt();

        assertThat(after).isAfterOrEqualTo(before);
    }

    @Test
    void setLastActiveAt_ShouldSetTime() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        java.time.Instant newTime = java.time.Instant.now().minusSeconds(60);
        session.setLastActiveAt(newTime);

        assertThat(session.getLastActiveAt()).isEqualTo(newTime);
    }

    @Test
    void setWinner_ShouldSetWinner() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        session.setWinner(playerX);
        assertThat(session.getWinner()).isEqualTo(playerX);
    }

    @Test
    void setStatus_ShouldSetStatus() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        session.setStatus(GameStatus.DRAW);
        assertThat(session.getStatus()).isEqualTo(GameStatus.DRAW);
    }

    @Test
    void setCurrentPlayer_ShouldSetCurrentPlayer() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        UUID newCurrent = UUID.randomUUID();
        session.setCurrentPlayer(newCurrent);
        assertThat(session.getCurrentPlayer()).isEqualTo(newCurrent);
    }

    @Test
    void setPlayerX_ShouldSetPlayerX() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        UUID newPlayerX = UUID.randomUUID();
        session.setPlayerX(newPlayerX);
        assertThat(session.getPlayerX()).isEqualTo(newPlayerX);
    }

    @Test
    void setPlayerO_ShouldSetPlayerO() {
        GameMap map = new GameMap(3);
        UUID playerX = UUID.randomUUID();
        GameSession session = new GameSession(map, playerX, true);

        UUID newPlayerO = UUID.randomUUID();
        session.setPlayerO(newPlayerO);
        assertThat(session.getPlayerO()).isEqualTo(newPlayerO);
    }
}
