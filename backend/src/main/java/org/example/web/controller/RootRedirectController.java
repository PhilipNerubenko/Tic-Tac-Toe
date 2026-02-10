package org.example.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Простая вспомогательная точка входа для удобства разработки.
 * Перенаправляет корневой путь на Swagger UI.
 */
@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}
