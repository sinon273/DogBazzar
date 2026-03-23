package com.example.DogBazzar.Listing;

import org.springframework.stereotype.Component;

@Component
public class ListingMapper {
    public ListingEntity toEntity(Listing listing){
        return new ListingEntity(
                listing.price(),
                listing.status(),
                listing.createdAt()
        );
    }
    public Listing toDomain(ListingEntity entity){
        return new Listing(
                entity.getId(),
                entity.getDog().getId(),
                entity.getPrice(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getSeller().getUsername(),
                entity.getDog().getName()
        );
    }
}
