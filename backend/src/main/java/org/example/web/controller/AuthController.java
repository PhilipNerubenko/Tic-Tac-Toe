package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.exception.DuplicateUserException;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.example.web.model.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST-контроллер для авторизации пользователей.
 * Обеспечивает API для регистрации и аутентификации.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Управление авторизацией пользователей")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Конструктор для инициализации контроллера.
     * <p>
     * Spring автоматически внедряет (Inject) необходимые зависимости
     * для работы с бизнес-логикой авторизации.
     *
     * @param authService сервис для обработки авторизации.
     */
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request запрос на регистрацию с логином и паролем.
     * @return ResponseEntity с фактом успешной регистрации.
     */
    @PostMapping("/signup")
    @Operation(summary = "Регистрация пользователя", description = "Создает новую учетную запись пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "409", description = "Логин уже занят")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUpRequest request) {
        try {
            RegistrationCommand command = new RegistrationCommand(request.login(), request.password());
            boolean result = authService.signUp(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", result));
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Login already in use"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request"));
        }
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     *
     * @param authorization заголовок Authorization с base64(login:password).
     * @return ResponseEntity с UUID аутентифицированного пользователя.
     */
    @PostMapping("/signin")
    @Operation(summary = "Авторизация пользователя", description = "Аутентифицирует пользователя и возвращает его UUID")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован")
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public ResponseEntity<Map<String, UUID>> signIn(@RequestHeader("Authorization") String authorization) {
        try {
            // Декодируем base64(login:password)
            String decoded = new String(java.util.Base64.getDecoder().decode(authorization.replace("Basic ", "")));
            String[] credentials = decoded.split(":", 2);

            if (credentials.length != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String login = credentials[0];
            String password = credentials[1];

            UUID userId = authService.signIn(login, password);
            return ResponseEntity.ok(Map.of("userId", userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Получает информацию о пользователе по UUID.
     *
     * @param id UUID пользователя.
     * @return ResponseEntity с информацией о пользователе.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @Operation(summary = "Получить информацию о пользователе", description = "Возвращает информацию о пользователе по его UUID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @ApiResponse(responseCode = "403", description = "Нет доступа к профилю другого пользователя")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.id());
            userInfo.put("login", user.login());
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
