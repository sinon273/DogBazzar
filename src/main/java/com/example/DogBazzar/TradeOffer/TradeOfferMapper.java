package com.example.DogBazzar.TradeOffer;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.Listing.ListingEntity;
import com.example.DogBazzar.User.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class TradeOfferMapper {
    public TradeOffer entity(OfferDto dto,
                             UserEntity buyer,
                             UserEntity seller,
                             ListingEntity listing,
                             DogEntity offeredDog){
        return new TradeOffer(
                null,
                buyer,
                seller,
                listing,
                offeredDog,
                dto.offer(),
                dto.status(),
                dto.createdAt(),
                dto.respondedAt()
        );
    }

    public OfferDto toDomain(TradeOffer entity){
        return new OfferDto(
                entity.getId(),
                entity.getListing().getId(),
                entity.getBuyer().getId(),
                entity.getBuyer().getUsername(),
                entity.getListing().getSeller().getId(),
                entity.getListing().getSeller().getUsername(),
                entity.getOfferedDog().getId(),
                entity.getOfferedDog().getName(),
                entity.getOffer(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }
}
