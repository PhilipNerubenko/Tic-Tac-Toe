package org.example.domain.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class JwtAuthentication implements Authentication {

    private final UUID id;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated;

    public JwtAuthentication(UUID id, Collection<? extends GrantedAuthority> authorities, boolean authenticated) {
        this.id = id;
        this.authorities = authorities;
        this.authenticated = authenticated;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public UUID getPrincipal() {
        return id;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return id != null ? id.toString() : null;
    }
}
