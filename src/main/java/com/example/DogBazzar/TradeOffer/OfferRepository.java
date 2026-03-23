package com.example.DogBazzar.TradeOffer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<TradeOffer,Long> {
    List<TradeOffer> findByListingIdAndStatus(Long id, TradeOfferStatus tradeOfferStatus);
}
