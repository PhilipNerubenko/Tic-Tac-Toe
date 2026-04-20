package org.example.domain.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority{
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
