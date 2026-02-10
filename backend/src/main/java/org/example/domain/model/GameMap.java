package org.example.domain.model;

/**
 * Доменная модель игрового поля для игры в "Крестики-нолики".
 * <p>
 * Класс инкапсулирует состояние сетки и предоставляет методы для безопасного
 * взаимодействия с ячейками поля, предотвращая некорректные ходы.
 */
public class GameMap {

    /** Двумерный массив, хранящий числовые значения состояний ячеек */
    private final int[][] map;

    /** Размер стороны квадратного поля */
    private final int size;

    /**
     * Создает пустое игровое поле заданного размера.
     *
     * @param size размер стороны поля.
     */
    public GameMap(int size) {
        this.size = size;
        this.map = new int[size][size];
    }

    /**
     * Создает игровое поле на основе существующего массива данных.
     * Используется преимущественно мапперами при восстановлении сессии.
     *
     * @param map массив состояний ячеек.
     * @param size размер стороны поля.
     */
    public GameMap(int[][] map, int size) {
        this.size = size;
        this.map = map;
    }

    /**
     * Конструктор по умолчанию. Создает классическое поле 3x3.
     */
    public GameMap() {
        this(3);
    }

    /**
     * Возвращает копию игрового поля.
     * <p>
     * <b>Важно:</b> Метод выполняет глубокое копирование массива, чтобы
     * вызывающий код не мог изменить внутреннее состояние поля напрямую.
     *
     * @return двумерный массив (копия текущего состояния).
     */
    public int[][] getMap() {
        int[][] copy = new int[size][size];
        for (int i = 0; i < size; i++) {
            copy[i] = map[i].clone();
        }
        return copy;
    }

    /**
     * Возвращает размер игрового поля.
     * @return размер стороны поля.
     */
    public int getSize() {
        return size;
    }

    /**
     * Устанавливает значение в указанную ячейку поля.
     *
     * @param row  индекс строки (0 до size-1).
     * @param col  индекс столбца (0 до size-1).
     * @param type тип устанавливаемого значения (крестик, нолик или пусто).
     * @throws IllegalArgumentException если координаты выходят за пределы игрового поля.
     */
    public void setCellValue(int row, int col, CellType type) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IllegalArgumentException("Coordinates are out of bounds.");
        }
        map[row][col] = type.getValue();
    }
}