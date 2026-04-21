package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.exception.DuplicateUserException;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.RegistrationCommand;
import org.example.domain.model.User;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.example.web.model.JwtRequest;
import org.example.web.model.JwtResponse;
import org.example.web.model.RefreshJwtRequest;
import org.example.web.model.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Operation(summary = "Регистрация пользователя", description = "Создает новую учетную запись пользователя и возвращает токены")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "409", description = "Логин уже занят")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUpRequest request) {
        try {
            RegistrationCommand command = new RegistrationCommand(request.login(), request.password());

            authService.signUp(command);

            JwtRequest authRequest = new JwtRequest(request.login(), request.password());
            JwtResponse jwtResponse = authService.signIn(authRequest);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("type", jwtResponse.type());
            responseBody.put("accessToken", jwtResponse.accessToken());
            responseBody.put("refreshToken", jwtResponse.refreshToken());

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Login already in use"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     *
     * @param request запрос с логином и паролем.
     * @return ResponseEntity с JWT-токенами.
     */
    @PostMapping("/signin")
    @Operation(summary = "Авторизация пользователя", description = "Аутентифицирует пользователя и возвращает JWT-токены")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован")
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public ResponseEntity<Map<String, String>> signIn(@RequestBody JwtRequest request) {
        try {
            JwtResponse jwtResponse = authService.signIn(request);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("type", jwtResponse.type());
            responseBody.put("accessToken", jwtResponse.accessToken());
            responseBody.put("refreshToken", jwtResponse.refreshToken());
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Обновляет access-токен по refresh-токену.
     *
     * @param request запрос с refresh-токеном.
     * @return ResponseEntity с новыми JWT-токенами.
     */
    @PostMapping("/refresh/access")
    @Operation(summary = "Обновление accessToken", description = "Обновляет access-токен по refresh-токену")
    @ApiResponse(responseCode = "200", description = "Токены успешно обновлены")
    @ApiResponse(responseCode = "401", description = "Невалидный refresh-токен")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody RefreshJwtRequest request) {
        try {
            JwtResponse jwtResponse = authService.refreshAccessToken(request.refreshToken());
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("type", jwtResponse.type());
            responseBody.put("accessToken", jwtResponse.accessToken());
            responseBody.put("refreshToken", jwtResponse.refreshToken());
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Обновляет refresh-токен по текущему refresh-токену.
     *
     * @param request запрос с refresh-токеном.
     * @return ResponseEntity с новыми JWT-токенами.
     */
    @PostMapping("/refresh/refresh")
    @Operation(summary = "Обновление refreshToken", description = "Обновляет refresh-токен по текущему refresh-токену")
    @ApiResponse(responseCode = "200", description = "Токены успешно обновлены")
    @ApiResponse(responseCode = "401", description = "Невалидный refresh-токен")
    public ResponseEntity<Map<String, String>> refreshRefreshToken(@RequestBody RefreshJwtRequest request) {
        try {
            JwtResponse jwtResponse = authService.refreshRefreshToken(request.refreshToken());
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("type", jwtResponse.type());
            responseBody.put("accessToken", jwtResponse.accessToken());
            responseBody.put("refreshToken", jwtResponse.refreshToken());
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Получает информацию о пользователе по accessToken.
     *
     * @return ResponseEntity с информацией о пользователе.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Получить информацию о текущем пользователе", description = "Возвращает информацию о пользователе по его accessToken")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<Map<String, Object>> getUserByAccessToken() {
        JwtAuthentication auth = (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = auth.getPrincipal();
        Optional<User> userOptional = userService.findById(userId);
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

    /**
     * Получает информацию о пользователе по UUID.
     *
     * @param id UUID пользователя.
     * @return ResponseEntity с информацией о пользователе.
     */
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
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
