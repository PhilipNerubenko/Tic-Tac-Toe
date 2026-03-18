package org.example.di.config;

import org.example.domain.service.UserService;
import org.example.web.filter.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности Spring Security.
 * <p>
 * Определяет правила доступа к endpoint'ам и настраивает фильтр авторизации.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Создает Bean для получения SecurityFilterChain.
     * <p>
     * Разрешает доступ без авторизации к endpoint'ам регистрации и авторизации.
     * Для всех остальных endpoint'ов требуется авторизация.
     * Использует AuthFilter в качестве фильтра.
     *
     * @param http        объект HttpSecurity для настройки безопасности.
     * @param userService сервис для работы с пользователями.
     * @return настроенный SecurityFilterChain.
     * @throws Exception если возникает ошибка при настройке безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/signin").permitAll()
                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())

                .addFilterBefore(new AuthFilter(userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
