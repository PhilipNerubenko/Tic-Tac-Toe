package org.example.di.config;

import org.example.datasource.repository.GameRepositoryImpl;
import org.example.datasource.repository.JpaGameRepository;
import org.example.datasource.repository.JpaUserRepository;
import org.example.datasource.repository.UserRepositoryImpl;
import org.example.domain.repository.GameRepository;
import org.example.domain.repository.UserRepository;
import org.example.domain.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    void gameService_ShouldCreateGameServiceImpl() {
        GameConfig config = new GameConfig();
        GameRepository repo = Mockito.mock(GameRepository.class);

        GameService service = config.gameService(repo);

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

        UserService service = config.userService(repo);

        assertNotNull(service);
        assertInstanceOf(UserServiceImpl.class, service);
    }

    @Test
    void authService_ShouldCreateAuthServiceImpl() {
        GameConfig config = new GameConfig();
        UserService userService = Mockito.mock(UserService.class);

        AuthService service = config.authService(userService);

        assertNotNull(service);
        assertInstanceOf(AuthServiceImpl.class, service);
    }
}
