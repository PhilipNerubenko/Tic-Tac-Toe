package org.example.domain.model;

import java.util.UUID;

/**
 * Основная доменная модель пользователя системы.
 * <p>
 * Данный класс представляет собой "чистую" бизнес-сущность.
 * Он независим от механизмов хранения (JPA/Hibernate) и
 * протоколов передачи данных (REST/JSON).
 *
 * @param id Уникальный идентификатор пользователя
 * @param login Уникальное имя (логин) для входа в систему
 * @param password * Пароль пользователя.
 * @param symbol  Кем играет игрок.
 * В текущей реализации хранится в открытом виде.
 * В дальнейшем планируется переход на BCrypt хеширование.
 */

public record User(UUID id, String login, String password, CellType symbol) {

    @Override
    public String toString() {

        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='[PROTECTED]'" +
                ", symbol=" + symbol +
                '}';
    }
}