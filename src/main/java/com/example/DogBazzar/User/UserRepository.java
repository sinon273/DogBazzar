package com.example.DogBazzar.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {


     public Optional<UserEntity> findByEmailIgnoreCase(String username);

    UserEntity findByEmail(String buyerEmail);
}
