package com.example.DogBazzar.Dog;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;

public record Dog(
        @Null
        Long id,
        @NotNull
        String name,
        @NotNull
        String breed,
        @NotNull
        String color,
        @NotNull
        BigDecimal price,
        @NotNull
        String imageUrl,
        @NotNull
        Rarity rarity

) {
}
