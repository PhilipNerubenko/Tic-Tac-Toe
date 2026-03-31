package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.model.GameSession;
import org.example.domain.model.User;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.example.web.mapper.GameMapperDTO;
import org.example.web.model.GameSessionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;

    /**
     * Конструктор для инициализации контроллера.
     * <p>
     * Spring автоматически внедряет (Inject) необходимые зависимости
     * для работы с бизнес-логикой и хранилищем данных.
     *
     * @param gameService    сервис для обработки игровой логики и ходов ИИ.
     * @param userService    сервис для работы с пользователями.
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
     * @param id             UUID сессии из URL.
     * @param userRequestDTO состояние поля после хода пользователя.
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
            @RequestBody GameSessionDTO userRequestDTO) {

        // 1. Получаем аутентифицированного пользователя из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String login = authentication.getName(); // principal - это логин (String) из AuthFilter
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        UUID authenticatedUserId = user.id();

        // 2. Маппим DTO из веба в чистую модель домена
        GameSession userMove = GameMapperDTO.toDomain(userRequestDTO);
        // Игнорируем currentPlayer из DTO - устанавливаем null, чтобы не было соблазна использовать его
        userMove.setCurrentPlayer(null);

        // 3. Вызов сервиса с проверкой авторизации
        GameSession updatedSession = gameService.executeTurn(id, userMove, authenticatedUserId);

        // 4. Возвращаем результат, маппим обратно в DTO
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

        // 1. Получаем аутентифицированного пользователя из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String login = authentication.getName(); // principal - это логин (String) из AuthFilter
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        UUID authenticatedUserId = user.id();

        // Проверяем, что пользователь пытается присоединиться как тот же пользователь, что и в параметре
        if (!authenticatedUserId.equals(guestId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Вы не можете присоединиться к игре за другого пользователя");
        }

        // Вызов сервиса для логики присоединения
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
        
        // 1. Получаем аутентифицированного пользователя из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String login = authentication.getName(); // principal - это логин (String) из AuthFilter
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        UUID authenticatedUserId = user.id();

        // 2. Получаем сессию из сервиса с проверкой авторизации
        GameSession session = gameService.findGameForUser(id, authenticatedUserId);
        
        return ResponseEntity.ok(GameMapperDTO.toDTO(session));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Получить активные игры", description = "Возвращает список доступных игр для присоединения")
    @ApiResponse(responseCode = "200", description = "Список активных игр")
    public ResponseEntity<java.util.Map<UUID, GameSessionDTO>> getActiveGames() {
        java.util.Map<UUID, GameSession> activeGames = gameService.getActiveGames();
        
        java.util.Map<UUID, GameSessionDTO> dtoMap = activeGames.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    java.util.Map.Entry::getKey,
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
        
        // 1. Получаем аутентифицированного пользователя из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String login = authentication.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        UUID authenticatedUserId = user.id();

        // 2. Проверяем, покинул ли соперник игру
        GameSession session = gameService.checkOpponentLeft(id, authenticatedUserId, timeoutSeconds);
        
        return ResponseEntity.ok(GameMapperDTO.toDTO(session));
    }
}