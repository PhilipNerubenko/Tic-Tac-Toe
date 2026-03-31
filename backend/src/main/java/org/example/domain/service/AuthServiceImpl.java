package org.example.domain.service;

import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Реализация интерфейса сервиса авторизации.
 * <p>
 * Обеспечивает бизнес-логику регистрации и аутентификации пользователей.
 */
public class AuthServiceImpl implements AuthService {

    /** Сервис для работы с пользователями */
    private final UserService userService;

    /**
     * Создает экземпляр сервиса авторизации.
     *
     * @param userService сервис для работы с пользователями.
     */
    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param command команда на регистрацию с логином и паролем.
     * @return {@code true}, если регистрация прошла успешно.
     * @throws IllegalArgumentException если пользователь с таким логином уже существует.
     */
    @Override
    @Transactional
    public boolean signUp(RegistrationCommand command) {
        userService.register(command.login(), command.password());
        return true;
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return UUID аутентифицированного пользователя.
     * @throws IllegalArgumentException если логин или пароль неверны.
     */
    @Override
    @Transactional(readOnly = true)
    public UUID signIn(String login, String password) {
        if (!userService.validateCredentials(login, password)) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        User user = userService.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));

        return user.id();
    }
}
