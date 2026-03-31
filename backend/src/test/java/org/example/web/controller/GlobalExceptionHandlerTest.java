package org.example.web.controller;

import org.example.domain.exception.GameNotFoundException;
import org.example.domain.exception.IntegrityViolationException;
import org.example.domain.exception.NotYourTurnException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {GameController.class, GlobalExceptionHandler.class}, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameController gameController;

    @Test
    void handleNotFound_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(gameController.getGameById(id)).thenThrow(new GameNotFoundException(id));

        mockMvc.perform(get("/game/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void handleViolation_ShouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        when(gameController.getGameById(id)).thenThrow(new IntegrityViolationException());

        mockMvc.perform(get("/game/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void handleNotYourTurn_ShouldReturn403() throws Exception {
        UUID id = UUID.randomUUID();
        when(gameController.getGameById(id)).thenThrow(new NotYourTurnException("Not your turn"));

        mockMvc.perform(get("/game/" + id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Not your turn"));
    }
}
