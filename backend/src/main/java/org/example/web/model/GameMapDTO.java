package org.example.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Объект передачи данных (DTO) для представления игрового поля.
 * <p>
 * Используется для сериализации состояния сетки в JSON-ответы API
 * и десериализации при получении обновленного состояния от клиента.
 */
public class GameMapDTO {

    /** * Двумерный массив ячеек.
     * В JSON представлен как массив массивов целых чисел.
     */
    @JsonProperty("map")
    private int[][] map;

    /** * Размер стороны квадратного поля.
     */
    @JsonProperty("size")
    private int size;

    /**
     * Конструктор по умолчанию.
     * Необходим библиотеке Jackson для корректной десериализации JSON в объект.
     */
    public GameMapDTO() {}

    /**
     * Создает заполненный DTO игрового поля.
     *
     * @param map массив состояний ячеек.
     * @param size размер поля.
     */
    public GameMapDTO(int[][] map, int size) {
        this.map = map;
        this.size = size;
    }

    /**
     * Возвращает массив состояний ячеек.
     * @return двумерный массив текущего состояния поля.
     */
    public int[][] getMap() { return map; }

    /**
     * Устанавливает массив состояний ячеек.
     * @param map устанавливаемый массив состояний.
     */
    public void setMap(int[][] map) { this.map = map; }

    /**
     * Возвращает размер стороны поля.
     * @return размер стороны поля.
     */
    public int getSize() { return size; }

    /**
     * Устанавливает размер стороны поля.
     * @param size устанавливаемый размер поля.
     */
    public void setSize(int size) { this.size = size; }
}