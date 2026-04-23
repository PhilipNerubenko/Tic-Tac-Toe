package org.example.domain.model;

import java.util.UUID;

public record PlayerStats(UUID userId, String login, double winRate) {}
