package com.example.DogBazzar.TradeOffer;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.Listing.ListingEntity;
import com.example.DogBazzar.User.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


//listing (объявление)

@Entity
@Table(name = "TradeOffers")
@EqualsAndHashCode(of = "id")
public class TradeOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "message",nullable = true)
    private String message;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TradeOfferStatus status;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "offer")
    private BigDecimal offer;
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @OneToOne
    @JoinColumn(name = "offered_dog_id")
    private DogEntity offeredDog;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private UserEntity buyer;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private ListingEntity listing;

    public TradeOffer(Long id, UserEntity buyer,UserEntity seller, ListingEntity listing, DogEntity offeredDog, BigDecimal offer, TradeOfferStatus status, LocalDateTime createdAt, LocalDateTime respondedAt) {
        this.id = id;
        this.buyer = buyer;
        this.buyer = seller;
        this.listing = listing;
        this.offeredDog = offeredDog;
        this.offer = offer;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public TradeOffer() {
    }


    public Long getId() {
        return id;
    }

    public BigDecimal getOffer() {
        return offer;
    }

    public void setOffer(BigDecimal offer) {
        this.offer = offer;
    }

    public DogEntity getOfferedDog() {
        return offeredDog;
    }

    public void setOfferedDog(DogEntity offeredDog) {
        this.offeredDog = offeredDog;
    }

    public UserEntity getBuyer() {
        return buyer;
    }

    public void setBuyer(UserEntity buyer) {
        this.buyer = buyer;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TradeOfferStatus getStatus() {
        return status;
    }

    public void setStatus(TradeOfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
