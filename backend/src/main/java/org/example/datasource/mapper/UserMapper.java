package org.example.datasource.mapper;

import org.example.datasource.model.UserEntity;
import org.example.datasource.model.UserRoleEntity;
import org.example.domain.model.CellType;
import org.example.domain.model.User;
import org.example.domain.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Компонент-преобразователь (Mapper) между доменной моделью пользователя и сущностями базы данных.
 * <p>
 * Служит для обеспечения архитектурной изоляции: изменения в структуре БД
 * не должны напрямую влиять на бизнес-логику приложения.
 */
public class UserMapper {

    /**
     * Конструктор по умолчанию.
     */
    private UserMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Преобразует сущность БД в объект доменной области.
     * Используется при извлечении данных из репозитория.
     *
     * @param userEntity сущность, полученная из источника данных.
     * @return объект {@link User} или {@code null}, если входные данные отсутствуют.
     */
    public static User toDomain(UserEntity userEntity) {
        if (userEntity == null) return null;

        List<UserRole> roles = userEntity.getRoles() == null
                ? Collections.singletonList(UserRole.USER)
                : userEntity.getRoles().stream()
                    .map(UserMapper::toDomainRole)
                    .collect(Collectors.toList());

        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getPassword(),
                CellType.CROSS,
                roles
        );
    }

    private static UserRole toDomainRole(UserRoleEntity roleEntity) {
        return UserRole.valueOf(roleEntity.name());
    }

    /**
     * Преобразует доменную модель пользователя в сущность для сохранения в БД.
     *
     * @param user объект доменной области.
     * @return сущность {@link UserEntity} или {@code null}, если объект пуст.
     */
    public static UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity userEntity = new UserEntity(user.login(), user.password());
        userEntity.setId(user.id());

        List<UserRoleEntity> roleEntities = user.roles() == null
                ? new ArrayList<>()
                : user.roles().stream()
                    .map(UserMapper::toEntityRole)
                    .collect(Collectors.toList());
        userEntity.setRoles(roleEntities);

        return userEntity;
    }

    private static UserRoleEntity toEntityRole(UserRole role) {
        return UserRoleEntity.valueOf(role.name());
    }
}
