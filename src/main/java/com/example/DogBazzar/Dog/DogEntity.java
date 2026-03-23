package com.example.DogBazzar.Dog;


import com.example.DogBazzar.Listing.ListingEntity;
import com.example.DogBazzar.TradeOffer.TradeOffer;
import com.example.DogBazzar.User.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "dogs")
@EqualsAndHashCode(of = "id")
public class DogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "breed",nullable = false)
    private String breed;
    @Column(name = "color")
    private String color;
    @Column(name = "price",nullable = false)
    private BigDecimal price;
    @Column(name = "imageUrl")
    private String imageUrl;
    @Column(name = "rarity",nullable = false)
    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToOne(mappedBy = "dog")
    private ListingEntity listing;

    @OneToOne(mappedBy = "offeredDog")
    private TradeOffer offer;

    public DogEntity(String name, String breed, String color, BigDecimal price, String imageUrl, Rarity rarity) {
        this.name = name;
        this.breed = breed;
        this.color = color;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
    }

    public DogEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public TradeOffer getOffer() {
        return offer;
    }

    public void setOffer(TradeOffer offer) {
        this.offer = offer;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", breed='" + breed + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", rarity=" + rarity +
                '}';
    }
}
