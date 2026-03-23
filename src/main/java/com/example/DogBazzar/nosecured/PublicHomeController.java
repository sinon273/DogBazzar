package com.example.DogBazzar.nosecured;

import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Dog.DogFilter;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.Listing.ListingFilter;
import com.example.DogBazzar.Listing.ListingStatus;
import com.example.DogBazzar.Dog.Rarity;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Listing.ListingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


//собаки отображаются
//детальная инфа о собаке
//должент быть метод с покупкой купить,если не авторизован то просит авторизоваться
//список активных объявлений
@RestController
@RequestMapping("/showcase")
public class PublicHomeController {

    private final DogService dogService;
    private final ListingService listingService;

    @Autowired
    public PublicHomeController(DogService dogService,ListingService listingService) {
        this.dogService = dogService;
        this.listingService = listingService;
    }

    @GetMapping
    public ResponseEntity<List<Dog>> getShowcase(
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Rarity rarity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        DogFilter filter = new DogFilter(
                breed,
                rarity,
                minPrice,
                maxPrice,
                null,
                true,
                (long) page,
                (long) size
        );

        List<Dog> dogs = dogService.searchDogs(filter);
        return ResponseEntity.ok(dogs);
    }
        @GetMapping("/{id}")
        public ResponseEntity<Dog> getDog(@PathVariable("id")Long id){
        Dog dog = dogService.getDogId(id);

        if(dog == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dog);
    }
    @GetMapping("/{id}/buy")
    public ResponseEntity<?> buyDog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Authentication required",
                            "message", "Please login to buy a dog"
                    ));
        }
        try {
            Listing purchasedListing = listingService.buyListing(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Dog purchased successfully",
                    "listingId", purchasedListing.id(),
                    "price", purchasedListing.price()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    //список активных объявлений
    @GetMapping("/listings")
    public ResponseEntity<List<Listing>> getActiveListings(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        ListingFilter filter = new ListingFilter(
                ListingStatus.ACTIVE,
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
