package org.example.datasource.mapper;

import org.example.datasource.model.UserEntity;
import org.example.domain.model.CellType;
import org.example.domain.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void privateConstructorTest() throws Exception {
        Constructor<UserMapper> constructor = UserMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    void toDomain_ShouldReturnNull_WhenEntityNull() {
        assertNull(UserMapper.toDomain(null));
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDomainNull() {
        assertNull(UserMapper.toEntity(null));
    }

    @Test
    void toDomain_ShouldConvertEntityToDomain() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity("testuser", "testpass");
        entity.setId(id);

        User domain = UserMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.id());
        assertEquals("testuser", domain.login());
        assertEquals("testpass", domain.password());
        assertEquals(CellType.CROSS, domain.symbol());
    }

    @Test
    void toEntity_ShouldConvertDomainToEntity() {
        UUID id = UUID.randomUUID();
        User domain = new User(id, "testuser", "testpass", CellType.CROSS);

        UserEntity entity = UserMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("testuser", entity.getLogin());
        assertEquals("testpass", entity.getPassword());
    }
}
