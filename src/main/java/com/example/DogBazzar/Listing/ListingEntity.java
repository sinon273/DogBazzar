package com.example.DogBazzar.Listing;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.TradeOffer.TradeOffer;
import com.example.DogBazzar.User.UserEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


//объявление
@Entity
@Table(name = "listing")
@EqualsAndHashCode(of = "id")
public class ListingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ListingStatus status;
    @Column(name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity seller;

    @OneToOne
    @JoinColumn(name = "dog_id")
    private DogEntity dog;

    @OneToMany(mappedBy = "listing")
    private List<TradeOffer> offers = new ArrayList<>();

    public ListingEntity(BigDecimal price, ListingStatus status, LocalDateTime createdAt) {
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public ListingEntity() {
    }

    public List<TradeOffer> getOffers() {
        return offers;
    }

    public UserEntity getSeller() {
        return seller;
    }

    public void setSeller(UserEntity seller) {
        this.seller = seller;
    }

    public DogEntity getDog() {
        return dog;
    }

    public void setDog(DogEntity dog) {
        this.dog = dog;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "id=" + id +
                ", price=" + price +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
