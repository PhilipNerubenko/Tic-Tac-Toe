package org.example.di.config;

import org.example.datasource.repository.GameRepositoryImpl;
import org.example.datasource.repository.JpaGameRepository;
import org.example.datasource.repository.JpaUserRepository;
import org.example.datasource.repository.UserRepositoryImpl;
import org.example.domain.repository.GameRepository;
import org.example.domain.repository.UserRepository;
import org.example.domain.service.*;
import org.example.web.filter.AuthFilter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Конфигурационный класс Spring для настройки компонентов игры.
 * <p>
 * Здесь определяется порядок создания и внедрения зависимостей (Dependency Injection).
 * Порядок инициализации: Storage -> Repository -> Service.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.example.datasource.repository")
@EntityScan(basePackages = "org.example.datasource.model")
public class GameConfig {

    @Bean
    public GameRepository gameRepository(JpaGameRepository storage) {
        return new GameRepositoryImpl(storage);
    }

    @Bean
    public AiMoveStrategy aiMoveStrategy() {
        return new MinimaxAiStrategy();
    }

    @Bean
    public GameService gameService(GameRepository repository, AiMoveStrategy aiMoveStrategy) {
        return new GameServiceImpl(repository, aiMoveStrategy);
    }

    @Bean
    public UserRepository userRepository(JpaUserRepository storage) {
        return new UserRepositoryImpl(storage);
    }

    @Bean
    public UserService userService(UserRepository repository) {
        return new UserServiceImpl(repository);
    }

    @Bean
    public AuthService authService(UserService userService) {
        return new AuthServiceImpl(userService);
    }

    @Bean
    public AuthFilter authFilter(UserService userService) {
        return new AuthFilter(userService);
    }
}