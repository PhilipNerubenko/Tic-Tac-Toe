package org.example.di.config;

import org.example.web.filter.AuthFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void securityFilterChain_ShouldConfigureCorrectly() throws Exception {
        SecurityConfig config = new SecurityConfig();
        AuthFilter authFilter = Mockito.mock(AuthFilter.class);
        HttpSecurity http = mock(HttpSecurity.class);
        when(http.csrf(any())).thenReturn(http);
        when(http.cors(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.httpBasic(any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.addFilterBefore(any(AuthFilter.class), eq(UsernamePasswordAuthenticationFilter.class))).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        var result = config.securityFilterChain(http, authFilter);

        assertNotNull(result);
        verify(http).csrf(any());
        verify(http).cors(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).httpBasic(any());
        verify(http).formLogin(any());
        verify(http).addFilterBefore(any(AuthFilter.class), eq(UsernamePasswordAuthenticationFilter.class));
    }
}
