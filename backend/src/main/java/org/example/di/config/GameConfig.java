package org.example.di.config;

import org.example.datasource.repository.GameRepositoryImpl;
import org.example.datasource.repository.JpaGameRepository;
import org.example.datasource.repository.JpaUserRepository;
import org.example.datasource.repository.UserRepositoryImpl;
import org.example.domain.repository.GameRepository;
import org.example.domain.repository.UserRepository;
import org.example.domain.service.AuthService;
import org.example.domain.service.AuthServiceImpl;
import org.example.domain.service.GameService;
import org.example.domain.service.GameServiceImpl;
import org.example.domain.service.UserService;
import org.example.domain.service.UserServiceImpl;
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

    /**
     * Создает экземпляр конфигурации.
     */
    public GameConfig() {
        // Конструктор по умолчанию
    }

    /**
     * Создает репозиторий, связывая его с хранилищем.
     * Используется абстракция {@link GameRepository} для изоляции слоя данных.
     *
     * @param storage внедренное хранилище данных
     * @return реализация репозитория {@link GameRepositoryImpl}
     */
    @Bean
    public GameRepository gameRepository(JpaGameRepository storage) {
        return new GameRepositoryImpl(storage);
    }

    /**
     * Создает основной сервис бизнес-логики игры.
     * Это "входная точка" для взаимодействия с игровыми механиками.
     *
     * @param repository внедренный репозиторий для управления состоянием сессий
     * @return реализация сервиса {@link GameServiceImpl}
     */
    @Bean
    public GameService gameService(GameRepository repository) {
        return new GameServiceImpl(repository);
    }

    /**
     * Создает репозиторий пользователей, связывая его с хранилищем.
     * Используется абстракция {@link UserRepository} для изоляции слоя данных.
     *
     * @param storage внедренное хранилище данных
     * @return реализация репозитория {@link UserRepositoryImpl}
     */
    @Bean
    public UserRepository userRepository(JpaUserRepository storage) {
        return new UserRepositoryImpl(storage);
    }

    /**
     * Создает основной сервис бизнес-логики пользователей.
     * Это "входная точка" для взаимодействия с пользователями системы.
     *
     * @param repository внедренный репозиторий для управления пользователями
     * @return реализация сервиса {@link UserServiceImpl}
     */
    @Bean
    public UserService userService(UserRepository repository) {
        return new UserServiceImpl(repository);
    }

    /**
     * Создает сервис авторизации.
     * Обеспечивает регистрацию и аутентификацию пользователей.
     *
     * @param userService внедренный сервис для работы с пользователями
     * @return реализация сервиса {@link AuthServiceImpl}
     */
    @Bean
    public AuthService authService(UserService userService) {
        return new AuthServiceImpl(userService);
    }
}