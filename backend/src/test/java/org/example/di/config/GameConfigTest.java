package org.example.di.config;

import org.example.datasource.repository.GameRepositoryImpl;
import org.example.datasource.repository.JpaGameRepository;
import org.example.datasource.repository.JpaUserRepository;
import org.example.datasource.repository.UserRepositoryImpl;
import org.example.domain.repository.GameRepository;
import org.example.domain.repository.UserRepository;
import org.example.domain.service.*;
import org.example.web.filter.AuthFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest {

    @Test
    void gameRepository_ShouldCreateGameRepositoryImpl() {
        GameConfig config = new GameConfig();
        JpaGameRepository jpaRepo = Mockito.mock(JpaGameRepository.class);

        GameRepository repo = config.gameRepository(jpaRepo);

        assertNotNull(repo);
        assertInstanceOf(GameRepositoryImpl.class, repo);
    }

    @Test
    void aiMoveStrategy_ShouldCreateMinimaxAiStrategy() {
        GameConfig config = new GameConfig();

        AiMoveStrategy strategy = config.aiMoveStrategy();

        assertNotNull(strategy);
        assertInstanceOf(MinimaxAiStrategy.class, strategy);
    }

    @Test
    void gameService_ShouldCreateGameServiceImpl() {
        GameConfig config = new GameConfig();
        GameRepository repo = Mockito.mock(GameRepository.class);
        AiMoveStrategy aiStrategy = Mockito.mock(AiMoveStrategy.class);

        GameService service = config.gameService(repo, aiStrategy);

        assertNotNull(service);
        assertInstanceOf(GameServiceImpl.class, service);
    }

    @Test
    void userRepository_ShouldCreateUserRepositoryImpl() {
        GameConfig config = new GameConfig();
        JpaUserRepository jpaRepo = Mockito.mock(JpaUserRepository.class);

        UserRepository repo = config.userRepository(jpaRepo);

        assertNotNull(repo);
        assertInstanceOf(UserRepositoryImpl.class, repo);
    }

    @Test
    void userService_ShouldCreateUserServiceImpl() {
        GameConfig config = new GameConfig();
        UserRepository repo = Mockito.mock(UserRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        UserService service = config.userService(repo, passwordEncoder);

        assertNotNull(service);
        assertInstanceOf(UserServiceImpl.class, service);
    }

    @Test
    void authService_ShouldCreateAuthServiceImpl() {
        GameConfig config = new GameConfig();
        UserService userService = Mockito.mock(UserService.class);
        JwtProvider jwtProvider = Mockito.mock(JwtProvider.class);

        AuthService service = config.authService(userService, jwtProvider);

        assertNotNull(service);
        assertInstanceOf(AuthServiceImpl.class, service);
    }
}
