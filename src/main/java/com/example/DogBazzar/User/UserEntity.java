package com.example.DogBazzar.User;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.Listing.ListingEntity;
import com.example.DogBazzar.TradeOffer.TradeOffer;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username",nullable = false)
    private String username;
    @Column(name = "email",nullable = false)
    private String email;
    @Column(name = "password",nullable = false)
    private String password;
    @Column(name = "balance")
    private BigDecimal balance;
    @Column(name = "created_at",updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<DogEntity> dogs;

    @OneToMany(mappedBy = "seller")
    private List<ListingEntity> listings = new ArrayList<>();

    @OneToMany(mappedBy = "buyer")
    private List<TradeOffer> offers = new ArrayList<>();

    public UserEntity(Long id, String username, String email, String password, BigDecimal balance, LocalDateTime createdAt, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.balance = balance;
        this.createdAt = createdAt;
        this.role = role;
    }

    public UserEntity() {
    }

    public List<DogEntity> getDogs() {
        return dogs;
    }

    public void setDogs(List<DogEntity> dogs) {
        this.dogs = dogs;
    }

    public List<ListingEntity> getListings() {
        return listings;
    }

    public void setListings(List<ListingEntity> listings) {
        this.listings = listings;
    }

    public List<TradeOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<TradeOffer> offers) {
        this.offers = offers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserRole getRole() {
        return role;
    }



    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                '}';
    }
}
