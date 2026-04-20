package org.example.domain.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity:3600}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity:604800}") long refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        List<String> roles = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenValidity)))
                .claim("roles", roles)
                .issuer("example-app")
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenValidity)))
                .issuer("example-app")
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validate(token);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token);
    }

    private boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer("example-app")
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer("example-app")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}