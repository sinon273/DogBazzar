package com.example.DogBazzar.User;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record User(
        @Null
        Long id,
        @NotNull
        String name,
        @NotNull
        String email,
        @NotNull
        String password,
        UserRole role
) {


}
