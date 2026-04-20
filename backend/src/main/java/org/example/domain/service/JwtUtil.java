package org.example.domain.service;

import io.jsonwebtoken.Claims;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    public JwtAuthentication createAuthentication(Claims claims) {

        UUID id = UUID.fromString(claims.getSubject());
        Object rolesObj = claims.get("roles");
        List<UserRole> authorities = rolesObj instanceof List
                ? ((List<?>) rolesObj).stream()
                  .map(role -> UserRole.valueOf(role.toString()))
                  .collect(Collectors.toList())
                : List.of();
        return new JwtAuthentication(id, authorities, true);
    }
}
