package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.model.GameMap;
import org.example.domain.model.GameSession;
import org.example.domain.model.User;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.example.web.mapper.GameMapperDTO;
import org.example.web.model.GameSessionDTO;
import org.example.web.model.MoveRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST-контроллер для управления игровым процессом "Крестиков-ноликов".
 * Обеспечивает API для создания сессий и обработки ходов.
 */
@RestController
@RequestMapping("/game")
@Tag(name = "Game Controller", description = "Управление игровыми сессиями и ходами ИИ")
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    /**
     * Конструктор для инициализации контроллера.
     *
     * @param gameService сервис для обработки игровой логики и ходов ИИ.
     * @param userService сервис для работы с пользователями.
     */
    public GameController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
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
     * @param id    UUID сессии из URL.
     * @param moveRequest состояние поля после хода пользователя.
     * @return обновленное состояние сессии.
     * @throws ResponseStatusException 404 если игра не найдена, 400 если ход невалиден.
     */
    @PostMapping(value = "/{id}/move", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Сделать ход", description = "Принимает ход игрока (X) и возвращает ответный ход ИИ (0)")
    @ApiResponse(responseCode = "200", description = "Ход обработан")
    @ApiResponse(responseCode = "400", description = "Нарушена целостность поля или игра уже завершена")
    @ApiResponse(responseCode = "404", description = "Сессия с таким ID не найдена")
    public ResponseEntity<GameSessionDTO> playMove(
            @PathVariable UUID id,
            @RequestBody MoveRequest moveRequest) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        GameMap gameMap = GameMapperDTO.toGameMap(moveRequest);
        GameSession userMove = new GameSession(gameMap, authenticatedUserId, false);

        GameSession updatedSession = gameService.executeTurn(id, userMove, authenticatedUserId);

        return ResponseEntity.ok(GameMapperDTO.toDTO(updatedSession));
    }
    
    @PostMapping("/{id}/join")
    @Operation(summary = "Присоединиться к игре", description = "Добавляет второго игрока в существующую сессию")
    @ApiResponse(responseCode = "200", description = "Игрок успешно присоединен")
    @ApiResponse(responseCode = "400", description = "Игра уже заполнена или это игра с ИИ")
    @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    public ResponseEntity<GameSessionDTO> joinGame(
            @PathVariable UUID id,
            @RequestParam UUID guestId) {

        UUID authenticatedUserId = getAuthenticatedUserId();

        if (!authenticatedUserId.equals(guestId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете присоединиться к игре за другого пользователя");
        }

        GameSession joinedSession = gameService.joinPlayer(id, guestId);

        return ResponseEntity.ok(GameMapperDTO.toDTO(joinedSession));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Получить состояние игры", description = "Возвращает текущее состояние игры по её ID")
    @ApiResponse(responseCode = "200", description = "Состояние игры")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    public ResponseEntity<GameSessionDTO> getGameById(
            @PathVariable UUID id) {
        
        UUID authenticatedUserId = getAuthenticatedUserId();

        GameSession session = gameService.findGameForUser(id, authenticatedUserId);
        
        return ResponseEntity.ok(GameMapperDTO.toDTO(session));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Получить активные игры", description = "Возвращает список доступных игр для присоединения")
    @ApiResponse(responseCode = "200", description = "Список активных игр")
    public ResponseEntity<Map<UUID, GameSessionDTO>> getActiveGames() {
        Map<UUID, GameSession> activeGames = gameService.getActiveGames();
        
        Map<UUID, GameSessionDTO> dtoMap = activeGames.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> GameMapperDTO.toDTO(entry.getValue())
                ));
        
        return ResponseEntity.ok(dtoMap);
    }
    
    @PostMapping("/{id}/check-opponent-left")
    @Operation(summary = "Проверить, покинул ли соперник игру", description = "Проверяет, не покинул ли один из игроков игру. Если игрок не активен более заданного времени, игра завершается.")
    @ApiResponse(responseCode = "200", description = "Состояние игры после проверки")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    public ResponseEntity<GameSessionDTO> checkOpponentLeft(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "30") long timeoutSeconds) {
        
        UUID authenticatedUserId = getAuthenticatedUserId();

        GameSession session = gameService.checkOpponentLeft(id, authenticatedUserId, timeoutSeconds);
        
        return ResponseEntity.ok(GameMapperDTO.toDTO(session));
    }

    /**
     * Получает UUID аутентифицированного пользователя из контекста безопасности.
     *
     * @return UUID текущего пользователя.
     * @throws ResponseStatusException 401 если пользователь не аутентифицирован или не найден.
     */
    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String login = authentication.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        return user.id();
    }
}