package org.example.datasource.repository;

import org.example.datasource.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA репозиторий для работы с сущностями пользователей.
 * <p>
 * Использует Spring Data JPA для реализации стандартных операций CRUD.
 */
public interface JpaUserRepository extends CrudRepository<UserEntity, UUID> {

    /**
     * Находит пользователя по логину.
     *
     * @param login уникальное имя пользователя.
     * @return {@link Optional}, содержащий найденную сущность,
     * или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<UserEntity> findByLogin(String login);

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param login уникальное имя пользователя.
     * @return {@code true}, если пользователь с таким логином существует.
     */
    boolean existsByLogin(String login);
}
