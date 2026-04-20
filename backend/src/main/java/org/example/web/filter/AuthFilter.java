package org.example.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.domain.model.User;
import org.example.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthFilter extends GenericFilterBean {

    private final UserService userService;

    public AuthFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorizationHeader = httpRequest.getHeader("Authorization");

        // Если есть заголовок Basic Auth — пытаемся аутентифицировать
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authorizationHeader.substring("Basic ".length());
                String decoded = new String(Base64.getDecoder().decode(base64Credentials));
                String[] credentials = decoded.split(":", 2);

                if (credentials.length == 2) {
                    String login = credentials[0];
                    String password = credentials[1];

                    if (userService.validateCredentials(login, password)) {
                        Optional<User> userOpt = userService.findByLogin(login);
                        List<GrantedAuthority> authorities = userOpt.map(user -> 
                                user.roles().stream()
                                    .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                                    .collect(Collectors.toList())
                        ).orElse(Collections.emptyList());
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                login, null, authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        chain.doFilter(request, response);
                    } else {
                        // Валидация не прошла — возвращаем 401
                        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                } else {
                    // Некорректный формат credentials — возвращаем 401
                    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
            } catch (Exception e) {
                // Ошибка парсинга — возвращаем 401
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        } else {
            // Нет заголовка Authorization — просто продолжаем цепочку
            chain.doFilter(request, response);
        }
    }
}