package com.example.DogBazzar.Listing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Listing(
        @Null
        Long id,           // для ответа (при создании null)
        @NotNull
        Long dogId,             // ID собаки (обязательно при создании)
        @NotNull
        BigDecimal price,           // цена (обязательно)
        @Null
        ListingStatus status,       // для ответа (при создании null)
        @Null
        LocalDateTime createdAt,    // для ответа (при создании null)
        @Null
        String sellerName,          // для ответа (при создании null)
        @Null
        String dogName
) {
}
