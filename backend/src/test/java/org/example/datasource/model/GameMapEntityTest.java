package org.example.datasource.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameMapEntityTest {
    @Test
    void defaultConstructor_ShouldCreateThreeByThreeMap() {
        GameMapEntity entity = new GameMapEntity();

        assertEquals(3, entity.getSize(), "Default size should be 3");
        assertNotNull(entity.getMap(), "The map array should be initialized");
        assertEquals(3, entity.getMap().length, "Row count should be 3");
        assertEquals(3, entity.getMap()[0].length, "Column count should be 3");
    }

    @Test
    void sizeConstructor_ShouldCreateEmptyMapOfGivenSize() {
        int customSize = 5;
        GameMapEntity entity = new GameMapEntity(customSize);

        assertEquals(customSize, entity.getSize());
        assertEquals(customSize, entity.getMap().length);
        assertEquals(0, entity.getMap()[0][0]);
    }
}