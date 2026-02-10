package org.example.di.config;

import org.example.datasource.repository.GameRepositoryImpl;
import org.example.datasource.storage.GameStorage;
import org.example.domain.repository.GameRepository;
import org.example.domain.service.GameService;
import org.example.domain.service.GameServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс Spring для настройки компонентов игры.
 * <p>
 * Здесь определяется порядок создания и внедрения зависимостей (Dependency Injection).
 * Порядок инициализации: Storage -> Repository -> Service.
 */
@Configuration
public class GameConfig {

    /**
     * Создает экземпляр конфигурации.
     */
    public GameConfig() {
        // Конструктор по умолчанию
    }

    /**
     * Создает компонент низкоуровневого хранилища данных в оперативной памяти.
     * @return экземпляр {@link GameStorage}
     */
    @Bean
    public GameStorage gameStorage() {
        return new GameStorage();
    }

    /**
     * Создает репозиторий, связывая его с хранилищем.
     * Используется абстракция {@link GameRepository} для изоляции слоя данных.
     *
     * @param storage внедренное хранилище данных
     * @return реализация репозитория {@link GameRepositoryImpl}
     */
    @Bean
    public GameRepository gameRepository(GameStorage storage) {
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
}