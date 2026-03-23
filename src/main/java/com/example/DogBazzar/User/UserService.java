package com.example.DogBazzar.User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Autowired
    public UserService(UserRepository repository,UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void createUser(User user){

        var entityToSave = mapper.toEntity(user);
        entityToSave.setRole(UserRole.USER);
        entityToSave.setBalance(new BigDecimal(0));

        repository.save(entityToSave);
    }
    //получить всех
    //добавить фильтрацию и пагинацию
    public List<User> findAllUser(){
        List<UserEntity> entities = repository.findAll();
        return entities.stream()
                .map(mapper::toDto)
                .toList();
    }
    //найти одного по айди
    public User findById(Long id) {
        return repository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("user not found with id =" + id));
    }
    //удалить одного использовать спринг секьюрити
    public void deleteById(Long id){
        Long currentUserId = getCurrentId();
        if(!currentUserId.equals(id)){
            throw new AccessDeniedException("You can only delete your own account");
        }
        repository.deleteById(id);
    }
    //обновить и тут
    public void UpdateUserId(User user, Long id){
        Long currentUserId = getCurrentId();
        if(!currentUserId.equals(id)){
            throw new AccessDeniedException("You can only delete your own account");
        }
        UserEntity userEntity = repository
                .findById(id)
                .orElseThrow(()->new EntityNotFoundException("user not found with id =" + id));
        userEntity.setUsername(user.name());
        userEntity.setEmail(user.email());
        userEntity.setPassword(user.password());
    }
    public UserEntity getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    public Long getCurrentId(){
        return getCurrentUser().getId();
    }
    //пополнение баланса
    public BigDecimal replenishment(Long id,BigDecimal add){
        Long currentUserId = getCurrentId();
        if(!currentUserId.equals(id)){
            throw new AccessDeniedException("You can only delete your own account");
        }
        UserEntity userEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found with id =" + id));
        BigDecimal money = userEntity.getBalance().add(add);
        return money;
    }
    //поиск пользователя по email
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        return repository.findByEmailIgnoreCase(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("user not found with email =" + email));
    }


    // если пользователь покупает то надо как то и где то уменьшить баланс
}
