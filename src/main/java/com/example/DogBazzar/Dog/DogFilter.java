package com.example.DogBazzar.Dog;

import java.math.BigDecimal;

public record DogFilter(
        String breed,
        Rarity rarity,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean hasOwner,
        Boolean forSale,
        Long pageNumber,
        Long pageSize
) {
}
