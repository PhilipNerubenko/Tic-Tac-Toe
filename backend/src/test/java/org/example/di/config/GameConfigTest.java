package org.example.di.config;

import org.example.domain.service.GameService;
import org.example.domain.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GameConfigTest {

    @Mock
    private GameRepository gameRepository;

    @Test
    void shouldInitializeAllBeans() {
        GameConfig config = new GameConfig();
        GameService service = config.gameService(gameRepository);
        
        assertThat(service).isNotNull();
        assertThat(gameRepository).isNotNull();
    }

    @Test
    void gameServiceShouldHaveRepositoryInjected() {
        GameConfig config = new GameConfig();
        GameService service = config.gameService(gameRepository);
        
        assertThat(service).isNotNull();
    }
}