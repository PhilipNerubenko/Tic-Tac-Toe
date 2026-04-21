package org.example.web.model;

import java.util.UUID;

public record LeaderboardResponseDTO(UUID userId, String login, double winRate) {}
