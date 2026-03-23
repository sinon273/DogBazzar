package com.example.DogBazzar.Dog;

import org.springframework.stereotype.Component;

@Component
public class DogMapper {
    public DogEntity DogToEntity(Dog dog){
        if(dog == null){
            return null;
        }
        return new DogEntity(
                dog.name(),
                dog.breed(),
                dog.color(),
                dog.price(),
                dog.imageUrl(),
                dog.rarity()
        );
    }
    public Dog DogToDomain(DogEntity entity){
        if(entity == null){
            return null;
        }
        return new Dog(
                entity.getId(),
                entity.getName(),
                entity.getBreed(),
                entity.getColor(),
                entity.getPrice(),
                entity.getImageUrl(),
                entity.getRarity()
        );
    }
}
