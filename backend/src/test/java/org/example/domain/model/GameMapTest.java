package org.example.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameMapTest {

    @Test
    void getMap_ShouldReturnDeepCopy() {
        GameMap gameMap = new GameMap(3);
        int[][] externalMap = gameMap.getMap();

        externalMap[0][0] = 1;

        assertThat(gameMap.getMap()[0][0]).isEqualTo(0);
    }

    @Test
    void DefaultConstructorGameMap_ShouldCreateMap3x3_WhenCreateNewMapWithoutParameters() {
        GameMap gameMap = new GameMap();
        int sizeCurrent = gameMap.getSize();
        int sizeExpected = 3;

        assertEquals(sizeExpected, sizeCurrent);
    }

    @Test
    void setCellValue_ShouldThrowException_WhenCoordinatesOutOfBounds() {
        GameMap gameMap = new GameMap(3);

        assertThrows(IllegalArgumentException.class, () -> gameMap.setCellValue(-1, 0, CellType.CROSS));
        assertThrows(IllegalArgumentException.class, () -> gameMap.setCellValue(3, 3, CellType.CROSS));
        assertThrows(IllegalArgumentException.class, () -> gameMap.setCellValue(0, 3, CellType.CROSS));
        assertThrows(IllegalArgumentException.class, () -> gameMap.setCellValue(0, -1, CellType.CROSS));
    }

    @Test
    void setCellValue_ShouldUpdateValueCorrectly() {
        GameMap gameMap = new GameMap(3);
        gameMap.setCellValue(1, 1, CellType.ZERO);

        assertThat(gameMap.getMap()[1][1]).isEqualTo(CellType.ZERO.getValue());
    }
}