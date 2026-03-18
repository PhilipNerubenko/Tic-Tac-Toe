package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.service.AuthService;
import org.example.web.model.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    /**
     * Конструктор для инициализации контроллера.
     * <p>
     * Spring автоматически внедряет (Inject) необходимые зависимости
     * для работы с бизнес-логикой авторизации.
     *
     * @param authService сервис для обработки авторизации.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
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
    @ApiResponse(responseCode = "400", description = "Пользователь с таким логином уже существует")
    public ResponseEntity<Map<String, Boolean>> signUp(@RequestBody SignUpRequest request) {
        boolean result = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", result));
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
}
