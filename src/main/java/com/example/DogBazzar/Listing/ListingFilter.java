package com.example.DogBazzar.Listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListingFilter(
        ListingStatus status,
        LocalDateTime date,
        BigDecimal maxPrice,
        BigDecimal minPrice,
        Long pageNumber,
        Long pageSize
){
}
