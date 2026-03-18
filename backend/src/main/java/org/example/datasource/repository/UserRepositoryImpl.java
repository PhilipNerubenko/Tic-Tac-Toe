package org.example.datasource.repository;

import org.example.datasource.mapper.UserMapper;
import org.example.datasource.model.UserEntity;
import org.example.domain.model.User;
import org.example.domain.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация интерфейса репозитория для управления пользователями.
 * <p>
 * Данный класс отвечает за координацию между хранилищем {@link JpaUserRepository}
 * и механизмом преобразования данных {@link UserMapper}.
 */
public class UserRepositoryImpl implements UserRepository {

    /** Хранилище данных (например, в оперативной памяти или БД) */
    private final JpaUserRepository jpaUserRepository;

    /**
     * Создает экземпляр репозитория.
     * @param jpaUserRepository реализация хранилища данных.
     */
    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    /**
     * Сохраняет пользователя.
     * Преобразует доменную модель в сущность БД перед сохранением.
     *
     * @param user доменная модель пользователя.
     */
    @Override
    public void save(User user) {
        UserEntity userEntity = UserMapper.toEntity(user);
        jpaUserRepository.save(userEntity);
    }

    /**
     * Находит пользователя по его уникальному идентификатору.
     *
     * @param id UUID пользователя.
     * @return {@link Optional}, содержащий доменную модель пользователя,
     * или пустой Optional, если пользователь не найден.
     */
    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(UserMapper::toDomain);
    }

    /**
     * Находит пользователя по логину.
     *
     * @param login уникальное имя пользователя.
     * @return {@link Optional}, содержащий доменную модель пользователя,
     * или пустой Optional, если пользователь не найден.
     */
    @Override
    public Optional<User> findByLogin(String login) {
        return jpaUserRepository.findByLogin(login)
                .map(UserMapper::toDomain);
    }

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param login уникальное имя пользователя.
     * @return {@code true}, если пользователь с таким логином существует.
     */
    @Override
    public boolean existsByLogin(String login) {
        return jpaUserRepository.existsByLogin(login);
    }
}
