package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.repository.GameRepository;
import org.example.domain.service.GameService;
import org.example.web.mapper.GameMapperDTO;
import org.example.web.model.GameSessionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * REST-контроллер для управления игровым процессом "Крестиков-ноликов".
 * Обеспечивает API для создания сессий и обработки ходов.
 */
@RestController
@RequestMapping("/game")
@Tag(name = "Game Controller", description = "Управление игровыми сессиями и ходами ИИ")
public class GameController {

    private final GameService gameService;

    /**
     * Конструктор для инициализации контроллера.
     * <p>
     * Spring автоматически внедряет (Inject) необходимые зависимости
     * для работы с бизнес-логикой и хранилищем данных.
     *
     * @param gameService    сервис для обработки игровой логики и ходов ИИ.
     */
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Создает новую игровую сессию.
     *
     * @param size размер игрового поля (по умолчанию 3x3).
     * @return DTO созданной сессии с уникальным UUID.
     */
    @PostMapping
    @Operation(summary = "Создать новую игру", description = "Инициализирует пустое поле и сохраняет сессию")
    @ApiResponse(responseCode = "201", description = "Игра успешно создана")
    public ResponseEntity<GameSessionDTO> createGame(
        @RequestParam UUID creatorId,
        @RequestParam(defaultValue = "true") boolean vsAi,
        @Parameter(description = "Размер квадратного поля") @RequestParam(defaultValue = "3") int size) {

        GameSession newSession = gameService.createGame(size, creatorId, vsAi);

        return ResponseEntity.status(HttpStatus.CREATED).body(GameMapperDTO.toDTO(newSession));
    }

    /**
     * Принимает ход пользователя, проверяет его и выполняет ответный ход ИИ.
     *
     * @param id             UUID сессии из URL.
     * @param userRequestDTO состояние поля после хода пользователя.
     * @return обновленное состояние сессии.
     * @throws ResponseStatusException 404 если игра не найдена, 400 если ход невалиден.
     */
    @PostMapping("/{id}")
    @Operation(summary = "Сделать ход", description = "Принимает ход игрока (X) и возвращает ответный ход ИИ (0)")
    @ApiResponse(responseCode = "200", description = "Ход обработан")
    @ApiResponse(responseCode = "400", description = "Нарушена целостность поля или игра уже завершена")
    @ApiResponse(responseCode = "404", description = "Сессия с таким ID не найдена")
    public ResponseEntity<GameSessionDTO> playMove(
            @PathVariable UUID id,
            @RequestBody GameSessionDTO userRequestDTO) {

        // 1. Маппим DTO из веба в чистую модель домена
        GameSession userMove = GameMapperDTO.toDomain(userRequestDTO);

        // 2. ОДИН вызов сервиса. Вся магия (поиск, валидация, ход ИИ, сохранение) внутри!
        GameSession updatedSession = gameService.executeTurn(id, userMove);

        // 3. Возвращаем результат, маппим обратно в DTO
        return ResponseEntity.ok(GameMapperDTO.toDTO(updatedSession));
    }
}