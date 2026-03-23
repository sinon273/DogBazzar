package com.example.DogBazzar.secured;

import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.TradeOffer.OfferDto;
import com.example.DogBazzar.User.User;
import com.example.DogBazzar.User.UserEntity;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Listing.ListingService;
import com.example.DogBazzar.TradeOffer.OfferService;
import com.example.DogBazzar.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

//продажа собаки если авторизован(создать объявление)
//мои собак
//мои объявления
//снять с продажи
//исходящие офферы
//входящие офферы
//создать оффер
//принять оффер
//отклонить оффер
//контрпредложение
//отменить свой оффер
//мой профиль
//обновить профиль
//мой баланс
//пополнить баланс

@RestController
@RequestMapping("/account")
public class PrivateAccountController {

    private final DogService dogService;
    private final ListingService listingService;
    private final OfferService offerService;
    private final UserService userService;


    @Autowired
    public PrivateAccountController(DogService dogService, ListingService listingService, OfferService offerService, UserService userService) {
        this.dogService = dogService;
        this.listingService = listingService;
        this.offerService = offerService;
        this.userService = userService;
    }
    //мои собаки
    @GetMapping("/dogs")
    public ResponseEntity<List<Dog>> getMyDogs(){
        List<Dog> dogs = dogService.getMyDogs();
        return ResponseEntity.ok(dogs);
    }
    //создать объявление продажа собаки
    @PostMapping("/listings")
    public ResponseEntity<Listing> createListing(@RequestBody Listing listingDto){

        Listing created = listingService.createListing(listingDto, listingDto.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    // 3. Мои объявления
    @GetMapping("/listings")
    public ResponseEntity<List<Listing>> getMyListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        List<Listing> listings = listingService.getMyListings();
        return ResponseEntity.ok(listings);
    }
    // 4. Снять с продажи
    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> cancelListing(@PathVariable Long id){
        listingService.cancellationListing(id);
        return ResponseEntity.noContent().build();
    }
    // 5. Исходящие офферы (мои предложения)
    @GetMapping("/offers/outgoing")
    public ResponseEntity<List<OfferDto>> getOutgoingOffers(){
        List<OfferDto> offers = offerService.getMyOffers();
        return ResponseEntity.ok(offers);
    }
    // 6. Входящие офферы (предложения мне)
    @GetMapping("/offers/incoming")
    public ResponseEntity<List<OfferDto>> getIncomingOffers(){
        List<OfferDto> offers = offerService.getIncomingOffers();
        return ResponseEntity.ok(offers);
    }
    // 7. Создать оффер
    @PostMapping("/offers")
    public ResponseEntity<OfferDto> createOffer(@RequestBody OfferDto offerDto){
        OfferDto created = offerService.createOffer(offerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    // 8. Принять оффер
    @PostMapping("/offers/{id}/accept")
    public ResponseEntity<OfferDto> acceptOffer(@PathVariable Long id){
        OfferDto accepted = offerService.acceptOffer(id);
        return ResponseEntity.ok(accepted);
    }
    // 9. Отклонить оффер
    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<OfferDto> rejectOffer(@PathVariable Long id){
        OfferDto reject = offerService.rejectOffer(id);
        return ResponseEntity.ok(reject);
    }
    // 10. Контрпредложение
    @PostMapping("/offers/{id}/counter")
    public ResponseEntity<OfferDto> counterOffer(@PathVariable Long id,@RequestBody OfferDto counterDto){
        OfferDto counter = offerService.counterOffer(id,counterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(counter);
    }
    // 11. Отменить свой оффер
    @DeleteMapping("/offers/{id}")
    public ResponseEntity<Void> cancelOffer(@PathVariable Long id){
        offerService.cancelOffer(id);
        return ResponseEntity.noContent().build();
    }
    // 12. Мой профиль
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(){
        UserEntity currentUser = userService.getCurrentUser();

        User profile = new User(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                null,
                currentUser.getRole()
        );
        return ResponseEntity.ok(profile);
    }
    // 13. Обновить профиль
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User newUser){
        User userDto = new User(
                null,
                newUser.name(),
                newUser.email(),
                newUser.password(),
                null
        );
        userService.UpdateUserId(userDto, userService.getCurrentId());
        return getProfile();
    }
    // 14. Мой баланс
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(){
        BigDecimal balance = userService.getCurrentUser().getBalance();
        return ResponseEntity.ok(balance);
    }
    // 15. Пополнить баланс
    @PostMapping("/balance")
    public ResponseEntity<BigDecimal> addFunds(@RequestParam BigDecimal amount){
        BigDecimal newBalance = userService.replenishment(userService.getCurrentId(),amount);
        return ResponseEntity.ok(newBalance);
    }
   //16 Получить все офферы на конкретное объявление (для владельца)
    @GetMapping("/listings/{listingId}/offers")
    public ResponseEntity<List<OfferDto>> getOffersByListing(@PathVariable Long listingId){
        List<OfferDto> offers = offerService.getOffersByListing(listingId);
        return ResponseEntity.ok(offers);
    }
    @GetMapping("/offers/{id}")
    public ResponseEntity<OfferDto> getOfferById(@PathVariable Long id) {
        OfferDto offer = offerService.getOfferById(id);
        return ResponseEntity.ok(offer);
    }
}
