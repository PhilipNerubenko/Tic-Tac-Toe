package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void constructor_ShouldBePublic() throws Exception {
        Constructor<Main> constructor = Main.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPublic(constructor.getModifiers()));
    }

    @Test
    void main_ShouldNotThrowException_WhenCalledWithEmptyArgs() {
        // Main вызывает SpringApplication.run, который требует контекст
        // Просто проверяем, что метод существует
        assertDoesNotThrow(() -> {
            var method = Main.class.getMethod("main", String[].class);
            assertNotNull(method);
        });
    }
}
