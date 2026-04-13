package org.example.web.filter;

import org.example.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthFilterTest {

    private UserService userService;
    private AuthFilter authFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        authFilter = new AuthFilter(userService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_ShouldAuthenticate_WhenValidBasicAuth() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:pass".getBytes());
        request.addHeader("Authorization", "Basic " + credentials);
        when(userService.validateCredentials("user", "pass")).thenReturn(true);

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user");
    }

    @Test
    void doFilter_ShouldReturn401_WhenInvalidCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("user:wrong".getBytes());
        request.addHeader("Authorization", "Basic " + credentials);
        when(userService.validateCredentials("user", "wrong")).thenReturn(false);

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ShouldReturn401_WhenMalformedBase64() throws Exception {
        request.addHeader("Authorization", "Basic !!!invalid-base64!!!");

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_ShouldReturn401_WhenNoColonInCredentials() throws Exception {
        String credentials = Base64.getEncoder().encodeToString("nocolon".getBytes());
        request.addHeader("Authorization", "Basic " + credentials);

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_ShouldContinueChain_WhenNoAuthHeader() throws Exception {
        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ShouldContinueChain_WhenNonBasicAuth() throws Exception {
        request.addHeader("Authorization", "Bearer some-token");

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
