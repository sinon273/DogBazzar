package com.example.DogBazzar.User;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserMapper{

    public UserEntity toEntity(User userDto){
        if(userDto == null){
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(userDto.id());
        entity.setUsername(userDto.name());
        entity.setEmail(userDto.email());
        entity.setPassword(userDto.password());
        entity.setBalance(BigDecimal.ZERO);

        return entity;
    }

    public User toDto(UserEntity entity){
        if(entity == null){
            return null;
        }
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole()
        );
    }
}
