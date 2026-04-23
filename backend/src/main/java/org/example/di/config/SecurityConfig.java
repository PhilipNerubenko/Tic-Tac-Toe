package org.example.di.config;

import org.example.web.filter.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
     * @param http       объект HttpSecurity для настройки безопасности.
     * @param authFilter фильтр авторизации, внедрённый через DI.
     * @return настроенный SecurityFilterChain.
     * @throws Exception если возникает ошибка при настройке безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter authFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/signin", "/auth/refresh/access", "/auth/refresh/refresh").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())

                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Создает Bean для кодирования паролей.
     * Используется BCrypt для хеширования паролей при регистрации и проверке.
     *
     * @return настроенный PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
