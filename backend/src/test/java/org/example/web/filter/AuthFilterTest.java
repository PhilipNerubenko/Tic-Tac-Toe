package org.example.web.filter;

import io.jsonwebtoken.Claims;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.service.JwtProvider;
import org.example.domain.service.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthFilterTest {

    private JwtProvider jwtProvider;
    private JwtUtil jwtUtil;
    private AuthFilter authFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtProvider = Mockito.mock(JwtProvider.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        authFilter = new AuthFilter(jwtProvider, jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_ShouldAuthenticate_WhenValidBearerToken() throws Exception {
        String token = "valid-access-token";
        Claims claims = Mockito.mock(Claims.class);
        JwtAuthentication jwtAuth = Mockito.mock(JwtAuthentication.class);

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtProvider.validateAccessToken(token)).thenReturn(true);
        when(jwtProvider.getClaims(token)).thenReturn(claims);
        when(jwtUtil.createAuthentication(claims)).thenReturn(jwtAuth);

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(jwtAuth);
    }

    @Test
    void doFilter_ShouldReturn401_WhenInvalidToken() throws Exception {
        String token = "invalid-token";

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtProvider.validateAccessToken(token)).thenReturn(false);

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ShouldContinueChain_WhenNoAuthHeader() throws Exception {
        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_ShouldContinueChain_WhenNonBearerAuth() throws Exception {
        request.addHeader("Authorization", "Basic some-credentials");

        authFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
