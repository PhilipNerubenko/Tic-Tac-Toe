package org.example.datasource.model;

/**
 * Сущность игрового поля для хранения в источнике данных.
 * <p>
 * Представляет собой состояние сетки игры, где каждая ячейка
 * содержит целочисленное значение, соответствующее состоянию (пусто/X/O).
 */
public class GameMapEntity {

    /** Двумерный массив, представляющий координаты игрового поля */
    private final int[][] map;

    /** Размер стороны квадратного игрового поля */
    private final int size;

    /**
     * Создает сущность на основе существующего массива данных.
     *
     * @param map массив данных игрового поля.
     * @param size размер поля.
     */
    public GameMapEntity(int[][] map, int size) {
        this.map = map;
        this.size = size;
    }

    /**
     * Создает пустое игровое поле заданного размера.
     *
     * @param size размер стороны поля (например, 3 для поля 3x3).
     */
    public GameMapEntity(int size) {
        this.size = size;
        this.map = new int[size][size];
    }

    /**
     * Конструктор по умолчанию.
     * Создает стандартное игровое поле размером 3x3.
     */
    public GameMapEntity() {
        this(3);
    }

    /**
     * Возвращает двумерный массив текущего состояния поля.
     * @return двумерный массив текущего состояния поля.
     */
    public int[][] getMap() {
        return map;
    }

    /**
     * Возвращает размер стороны поля.
     * @return размер стороны поля.
     */
    public int getSize() {
        return size;
    }
}