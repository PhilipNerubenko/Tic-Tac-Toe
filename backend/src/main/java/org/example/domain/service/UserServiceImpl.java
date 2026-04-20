package org.example.domain.service;

import org.example.domain.exception.DuplicateUserException;
import org.example.domain.model.CellType;
import org.example.domain.model.User;
import org.example.domain.model.UserRole;
import org.example.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация интерфейса сервиса для управления пользователями системы.
 * <p>
 * Обеспечивает бизнес-логику регистрации, аутентификации и поиска пользователей.
 */
public class UserServiceImpl implements UserService {

    /** Репозиторий для работы с пользователями */
    private final UserRepository userRepository;

    /** Кодировщик паролей для безопасного хранения */
    private final PasswordEncoder passwordEncoder;

    /**
     * Создает экземпляр сервиса.
     *
     * @param userRepository    репозиторий для работы с пользователями.
     * @param passwordEncoder   кодировщик паролей.
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return созданный пользователь.
     * @throws DuplicateUserException если пользователь с таким логином уже существует.
     */
    @Override
    @Transactional
    public User register(String login, String password) {
        if (userRepository.existsByLogin(login)) {
            throw new DuplicateUserException("Login already in use");
        }

        User user = new User(UUID.randomUUID(), login, passwordEncoder.encode(password), CellType.CROSS, Collections.singletonList(UserRole.USER));
        userRepository.save(user);
        return user;
    }

    /**
     * Находит пользователя по логину.
     *
     * @param login уникальное имя пользователя.
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    /**
     * Находит пользователя по уникальному идентификатору.
     *
     * @param id UUID пользователя.
     * @return {@link Optional}, содержащий найденного пользователя,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param login уникальное имя пользователя.
     * @return {@code true}, если пользователь с таким логином существует.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    /**
     * Проверяет соответствие логина и пароля.
     *
     * @param login    уникальное имя пользователя.
     * @param password пароль пользователя.
     * @return {@code true}, если логин и пароль верны.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateCredentials(String login, String password) {
        Optional<User> user = userRepository.findByLogin(login);
        return user.isPresent() && passwordEncoder.matches(password, user.get().password());
    }
}
