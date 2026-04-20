package org.example.domain.service;

import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.web.model.JwtRequest;
import org.example.web.model.JwtResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

/**
 * Реализация интерфейса сервиса авторизации.
 * <p>
 * Обеспечивает бизнес-логику регистрации и аутентификации пользователей.
 */
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional
    public boolean signUp(RegistrationCommand command) {
        userService.register(command.login(), command.password());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse signIn(JwtRequest request) {
        if (!userService.validateCredentials(request.login(), request.password())) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        User user = userService.findByLogin(request.login())
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));

        return generateJwtResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse refreshAccessToken(String refreshToken) {
        User user = resolveUserFromRefreshToken(refreshToken);
        return generateJwtResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse refreshRefreshToken(String refreshToken) {
        User user = resolveUserFromRefreshToken(refreshToken);
        return generateJwtResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public JwtAuthentication getAuthentication(String accessToken) {
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new IllegalArgumentException("Invalid access token");
        }

        String userId = jwtProvider.getClaims(accessToken).getSubject();
        User user = userService.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Collection<? extends GrantedAuthority> authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

        return new JwtAuthentication(user.id(), authorities, true);
    }

    private User resolveUserFromRefreshToken(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userId = jwtProvider.getClaims(refreshToken).getSubject();
        return userService.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private JwtResponse generateJwtResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        return new JwtResponse("Bearer", accessToken, refreshToken);
    }
}
