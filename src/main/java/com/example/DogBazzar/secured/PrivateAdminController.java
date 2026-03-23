package com.example.DogBazzar.secured;

import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Dog.DogFilter;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.Listing.ListingFilter;
import com.example.DogBazzar.User.User;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Listing.ListingService;
import com.example.DogBazzar.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// все собаки (включая без хозяина)
// создать новую собаку
// редактировать собаку
// удалить собаку
// все пользователи
// пользователь
// все объявления (любые статусы)
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class PrivateAdminController {
    private final DogService dogService;
    private final UserService userService;
    private final ListingService listingService;

    @Autowired
    public PrivateAdminController(DogService dogService, UserService userService, ListingService listingService) {
        this.dogService = dogService;
        this.userService = userService;
        this.listingService = listingService;
    }
    @GetMapping("/dogs")
    public ResponseEntity<List<Dog>> getAllDogs(){
        DogFilter filter = new DogFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                0L,
                1000L
        );
        List<Dog> dogs = dogService.searchDogs(filter);
        return ResponseEntity.ok(dogs);
    }
    @PostMapping("/dogs")
    public ResponseEntity<Dog> createDog(@RequestBody Dog dogDto){
        Dog created = dogService.createDog(dogDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PutMapping("/dogs/{id}")
    public ResponseEntity<Dog> updateDog(@PathVariable Long id,@RequestBody Dog dogDto){
        Dog updated = dogService.updateDog(id,dogDto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/dogs/{id}")
    public ResponseEntity<Void> deleteDog(@PathVariable Long id){
        dogService.deleteDog(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        System.out.println("========== АДМИН КОНТРОЛЛЕР ==========");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Авторизация: " + auth);
        System.out.println("Принципал: " + (auth != null ? auth.getPrincipal() : "null"));
        System.out.println("Роли: " + (auth != null ? auth.getAuthorities() : "null"));

        List<User> users = userService.findAllUser();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/listings")
    public ResponseEntity<List<Listing>> getAllListings(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        ListingFilter filter = new ListingFilter(
                null,
                null,
                maxPrice,
                minPrice,
                (long) page,
                (long) size
        );
        List<Listing> listings = listingService.findAllListing(filter);
        return ResponseEntity.ok(listings);
    }
}
