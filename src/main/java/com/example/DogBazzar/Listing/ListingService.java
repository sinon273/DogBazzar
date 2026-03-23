package com.example.DogBazzar.Listing;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.TradeOffer.TradeOfferStatus;
import com.example.DogBazzar.User.UserEntity;
import com.example.DogBazzar.Dog.DogRepository;
import com.example.DogBazzar.User.UserRepository;
import com.example.DogBazzar.User.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class ListingService {

    private final ListingRepository repository;
    private final ListingMapper mapper;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final UserService userService;

    @Autowired
    public ListingService(ListingRepository repository, ListingMapper mapper, UserRepository userRepository, DogRepository dogRepository, UserService userService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.dogRepository = dogRepository;
        this.userService = userService;
    }

    public Listing createListing(Listing listing,Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!(auth != null && auth.isAuthenticated())){
            throw new AuthenticationCredentialsNotFoundException("The user is not authenticated");
        }

        DogEntity dogEntity = dogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dog not found"));

        String email = auth.getName();

        UserEntity userEntity = dogEntity.getUser();

        if(!userEntity.getEmail().equals(email)){
            throw new AccessDeniedException("You do not have the permissions to perform this operation");
        }
        if(dogEntity.getListing() != null){
            throw new IllegalStateException("This dog already has an active ad.");
        }
        ListingEntity entity = mapper.toEntity(listing);
        if(listing.status()!=null){
            throw new IllegalArgumentException("Статус не может быть установлен при создании");
        }
        if (dogEntity.getUser() == null || !dogEntity.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Not your dog");
        }
        if (dogEntity.getListing() != null && dogEntity.getListing().getStatus() == ListingStatus.ACTIVE) {
            throw new IllegalStateException("Dog already has active listing");
        }

        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus(ListingStatus.ACTIVE);
        entity.setSeller(userEntity);
        entity.setDog(dogEntity);

        repository.save(entity);

        return mapper.toDomain(entity);
    }
    //Получение конкретного объявления
    public Listing findListing(Long id){
        ListingEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Нет такого объявления"));
        return mapper.toDomain(entity);
    }
    //Снятие с продажи (отмена объявления)

    public void cancellationListing(Long id){
        ListingEntity entity = repository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Entity not found " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if(!email.equals(entity.getSeller().getEmail())){
            throw new AccessDeniedException("Вы можете управлять только своими объявлениями");
        }

        if(entity.getStatus() == ListingStatus.ACTIVE){
            entity.setStatus(ListingStatus.CANCELLED);
        }
        repository.save(entity);
    }
    //admin and user
    public List<Listing> findAllListing(ListingFilter filter){
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber().intValue() : 0;
        int pageSize = filter.pageSize() != null ? filter.pageSize().intValue() : 10;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));

        List<ListingEntity> entities;

        if(isAdmin){
            entities = repository.searchAllByFilter(
                    null,
                    filter.date(),
                    filter.maxPrice(),
                    filter.minPrice(),
                    pageable
            );
        }
        else{
            entities = repository.searchAllByFilter(
                    ListingStatus.ACTIVE,
                    filter.date(),
                    filter.maxPrice(),
                    filter.minPrice(),
                    pageable
            );
        }
        return entities
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    //buy
    public Listing buyListing(Long listingId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        ListingEntity listing = repository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + listingId));

        if(listing.getStatus() != ListingStatus.ACTIVE){
            throw new IllegalStateException("Listing is not active. Current status: " + listing.getStatus());

        }
        String buyerEmail = auth.getName();
        UserEntity buyer = userRepository.findByEmail(buyerEmail);

        UserEntity seller = listing.getSeller();
        if(seller.getId().equals(buyer.getId())){
            throw new IllegalStateException("You cannot buy your own dog");
        }
        DogEntity dog = listing.getDog();
        BigDecimal price = listing.getPrice();

        if(buyer.getBalance().compareTo(price) < 0){
            throw new IllegalStateException("Insufficient funds. Your balance: " + buyer.getBalance() +
                    ", required: " + price);
        }
        buyer.setBalance(buyer.getBalance().subtract(price));

        seller.setBalance(seller.getBalance().add(price));

        dog.setUser(buyer);
        listing.setStatus(ListingStatus.SOLD);

        if (listing.getOffers() != null && !listing.getOffers().isEmpty()) {
            listing.getOffers().stream()
                    .filter(offer -> offer.getStatus() == TradeOfferStatus.PENDING)
                    .forEach(offer -> offer.setStatus(TradeOfferStatus.CANCELLED));
        }

        userRepository.save(buyer);
        userRepository.save(seller);
        dogRepository.save(dog);
        repository.save(listing);

        return mapper.toDomain(listing);
    }
    public List<Listing> getMyListings(){
        Long currentUserId = userService.getCurrentId();

        List<ListingEntity> entities = repository.findBySellerId(currentUserId);
        return entities
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    

}
