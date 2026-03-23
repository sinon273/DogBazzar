package com.example.DogBazzar.Dog;

import com.example.DogBazzar.User.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Service
@Transactional
public class DogService {

    private final DogRepository repository;
    private final DogMapper mapper;

    @Autowired
    public DogService(DogRepository repository, DogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Dog createDog(Dog dog){
        DogEntity entity = mapper.DogToEntity(dog);
        entity.setUser(null);
        repository.save(entity);
        return mapper.DogToDomain(entity);
    }
    //admin and user (controller)
    public List<Dog> searchDogs(DogFilter filter){
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber().intValue() : 0;
        int pageSize = filter.pageSize() != null ? filter.pageSize().intValue() : 10;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));

        List<DogEntity> allEntities;

        if(!isAdmin && filter.forSale() == null){
             allEntities = repository.searchAllByFilter(
                    filter.breed(),
                    filter.rarity(),
                    filter.minPrice(),
                    filter.maxPrice(),
                    filter.hasOwner(),
                    true,
                    pageable
            );
        }else {
             allEntities = repository.searchAllByFilter(
                    filter.breed(),
                    filter.rarity(),
                    filter.minPrice(),
                    filter.maxPrice(),
                    filter.hasOwner(),
                    filter.forSale(),
                    pageable
            );
        }
        return allEntities.stream()
                .map(mapper::DogToDomain)
                .toList();
    }
    public Dog getDogId(Long id){
        DogEntity entity = repository.findById(id)
                .orElseThrow(()->new EntityNotFoundException(
                        "Not found dog by id =" + id
                ));
        return mapper.DogToDomain(entity);
    }

    public List<Dog> getMyDogs(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        String email = auth.getName();

        if(email == null){
            throw new RuntimeException("Authenticated user has no email");
        }
        List<DogEntity> entities = repository.findAllByUserEmail(email);
        return entities.stream()
                .map(mapper::DogToDomain)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<Dog> getDogsByUserId(Long userId){
        return repository.findByUserId(userId)
                .stream()
                .map(mapper::DogToDomain)
                .toList();
    }
//Обновление собаки (для админа)
    public Dog updateDog(Long id,Dog dog){
        //находим собаку
        DogEntity entity = repository.findById(id)
                .orElseThrow(()->new EntityNotFoundException(
                        "Not found dog by id =" + id));
        if(entity == null){
            throw new ResourceAccessException("Dog not found with id: " + id);
        }
        //получаем текущего пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        //проверяем админ ли
        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UserEntity owner = entity.getUser();
        if(isAdmin){
            entity.setBreed(dog.breed());
            entity.setColor(dog.color());
            entity.setPrice(dog.price());
            entity.setName(dog.name());
            entity.setImageUrl(dog.imageUrl());
            entity.setRarity(dog.rarity());
        }else if(!isAdmin){
            //проверяем что собака принадлежит текущему пользоваттел
            if(owner == null){
                throw new AccessDeniedException("This dog has no owner and cannot" +
                        " be modified by non-admin");
            }
            if(!owner.getEmail().equals(email)){
                throw new AccessDeniedException("You can only update your own dogs");
            }
            entity.setName(dog.name());
            entity.setPrice(dog.price());
        }
        repository.save(entity);
        return mapper.DogToDomain(entity);
    }
    // Удаление собаки (для админа)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDog(Long id){
        repository.deleteById(id);
    }
    //Статистика (для админа) количество свободных собак купленных


}
