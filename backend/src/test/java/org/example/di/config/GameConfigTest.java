package org.example.di.config;

import org.example.domain.service.GameService;
import org.example.domain.repository.GameRepository;
import org.example.datasource.storage.GameStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GameConfig.class)
class GameConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldInitializeAllBeans() {
        assertThat(context.getBean(GameStorage.class)).isNotNull();
        assertThat(context.getBean(GameRepository.class)).isNotNull();
        assertThat(context.getBean(GameService.class)).isNotNull();
    }

    @Test
    void gameServiceShouldHaveRepositoryInjected() {
        GameService service = context.getBean(GameService.class);
        assertThat(service).isNotNull();
    }
}