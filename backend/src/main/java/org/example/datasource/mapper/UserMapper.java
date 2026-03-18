package org.example.datasource.mapper;

import org.example.datasource.model.UserEntity;
import org.example.domain.model.CellType;
import org.example.domain.model.User;

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

        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getPassword(),
                CellType.CROSS // По умолчанию для новых пользователей
        );
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
        return userEntity;
    }
}
