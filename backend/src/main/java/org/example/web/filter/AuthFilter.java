package org.example.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

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

        String path = httpRequest.getServletPath();

        // 1. Пропускаем публичные эндпоинты
        if ("/auth/signup".equals(path) || "/auth/signin".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authorizationHeader.substring("Basic ".length());
                String decoded = new String(Base64.getDecoder().decode(base64Credentials));
                String[] credentials = decoded.split(":", 2);

                if (credentials.length == 2) {
                    String login = credentials[0];
                    String password = credentials[1];

                    if (userService.validateCredentials(login, password)) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                login,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                        // Устанавливаем аутентификацию в контекст для текущего потока/запроса
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        chain.doFilter(request, response);
                        return;
                    }
                }
            } catch (Exception e) {
                // Ошибка декодирования — игнорируем, выдадим 401 ниже
            }
        }

        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write("{\"error\": \"Unauthorized\"}");
    }
}