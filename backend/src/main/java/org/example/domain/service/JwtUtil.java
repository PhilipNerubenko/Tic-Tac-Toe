package org.example.domain.service;

import io.jsonwebtoken.Claims;
import org.example.domain.model.JwtAuthentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    public JwtAuthentication createAuthentication(Claims claims) {

        UUID id = UUID.fromString(claims.getSubject());
        Object rolesObj = claims.get("roles");
        List<String> roles = rolesObj instanceof List
                ? ((List<?>) rolesObj).stream()
                  .map(Object::toString)
                  .toList()
                : List.of();
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return new JwtAuthentication(id, authorities, true);
    }
}
