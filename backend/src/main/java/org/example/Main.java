package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения "Крестики-нолики".
 * <p>
 * Аннотация {@link SpringBootApplication} объединяет в себе:
 * <ul>
 * <li>{@code @Configuration}: позволяет регистрировать бины;</li>
 * <li>{@code @EnableAutoConfiguration}: включает автоматическую настройку Spring Boot;</li>
 * <li>{@code @ComponentScan}: запускает поиск компонентов в текущем пакете и его подпакетах.</li>
 * </ul>
 */
@SpringBootApplication
public class Main {

    /**
     * Конструктор по умолчанию.
     */
    public Main() {
    }

    /**
     * Точка входа в приложение.
     * Запускает встроенный сервер (Tomcat) и контекст Spring.
     *
     * @param args аргументы командной строки.
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}