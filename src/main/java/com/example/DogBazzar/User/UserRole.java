package com.example.DogBazzar.User;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    ADMIN,
    USER;

    public SimpleGrantedAuthority toAuthority(){
        return new SimpleGrantedAuthority("ROLE_"+this.name());
    }
}
