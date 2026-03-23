package com.example.DogBazzar.TradeOffer;

import com.example.DogBazzar.Dog.DogEntity;
import com.example.DogBazzar.Listing.ListingEntity;
import com.example.DogBazzar.Listing.ListingStatus;
import com.example.DogBazzar.User.UserEntity;
import com.example.DogBazzar.Dog.DogRepository;
import com.example.DogBazzar.Listing.ListingRepository;
import com.example.DogBazzar.User.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OfferService {

    private final OfferRepository repository;
    private final TradeOfferMapper mapper;
    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Autowired
    public OfferService(OfferRepository repository, TradeOfferMapper mapper, DogRepository dogRepository, UserRepository userRepository, ListingRepository listingRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.dogRepository = dogRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public String UserEmail(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        return auth.getName();

    }

    //Создание объявления
    public OfferDto createOffer(OfferDto offer) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        String buyerEmail = auth.getName();
        UserEntity buyer = userRepository.findByEmail(buyerEmail);

        Long listingId = offer.listingId();
        ListingEntity listingEntity = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found " + listingId));

        if (listingEntity.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is not active. Current status: " + listingEntity.getStatus());
        }
        UserEntity seller = listingEntity.getSeller();
        if (seller.getId().equals(buyer.getId())) {
            throw new IllegalStateException("You cannot make an offer on your own listing");
        }
        boolean hasDog = offer.dogId() != null;
        boolean hasMoney = offer.offer() != null && offer.offer().compareTo(BigDecimal.ZERO) > 0;

        if (!hasDog && !hasMoney) {
            throw new IllegalArgumentException("You must offer either a dog or money");
        }
        DogEntity offeredDog = null;
        if (hasDog) {
            offeredDog = dogRepository.findById(offer.dogId())
                    .orElseThrow(() -> new EntityNotFoundException("Dog not found with id: " + offer.dogId()));

            if (offeredDog.getUser() == null || !offeredDog.getUser().getId().equals(buyer.getId())) {
                throw new AccessDeniedException("You can only offer your own dogs");
            }
            if (offeredDog.getListing() != null && offeredDog.getListing().getStatus() == ListingStatus.ACTIVE) {
                throw new IllegalStateException("Cannot offer a dog that is already listed for sale");
            }
        }
        if(hasMoney && offer.offer().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Money offer must be positive");
        }
        TradeOffer offerEntity = new TradeOffer();
        offerEntity.setBuyer(buyer);
        offerEntity.setListing(listingEntity);
        offerEntity.setOfferedDog(offeredDog);
        offerEntity.setOffer(offer.offer());
        offerEntity.setMessage(offer.message());
        offerEntity.setStatus(TradeOfferStatus.PENDING);
        offerEntity.setCreatedAt(LocalDateTime.now());
        offerEntity.setRespondedAt(null);

        TradeOffer saved = repository.save(offerEntity);

        return mapper.toDomain(saved);
    }
    //получение по id
    public OfferDto getOfferById(Long id){
        TradeOffer offerEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found" + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isBuyer = offerEntity.getBuyer().getEmail().equals(currentUserEmail);
        boolean isSeller = offerEntity.getListing().getSeller().getEmail().equals(currentUserEmail);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isBuyer && !isSeller && !isAdmin){
            throw new AccessDeniedException("You don't have permission to view this offer");
        }

        return mapper.toDomain(offerEntity);
    }
    // 3. Получение офферов на объявление
    public List<OfferDto> getOffersByListing(Long listingId){
        ListingEntity listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
        UserEntity entity = userRepository.findByEmail(UserEmail());
        if(!listing.getSeller().getId().equals(entity.getId())){
            throw new AccessDeniedException("the user is not the owner of the ad");
        }
        return listing.getOffers().stream()
                .map(mapper::toDomain).toList();
    }
    //получение моих исхлдящих офферов
    public List<OfferDto> getMyOffers(){
        UserEntity entity = userRepository.findByEmail(UserEmail());
        return entity
                .getOffers()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    //входящие офферы
    public List<OfferDto> getIncomingOffers(){
        UserEntity entity = userRepository.findByEmail(UserEmail());

        List<ListingEntity> userListings  = listingRepository.findBySellerId(entity.getId());

        List<TradeOffer> allOffers = new ArrayList<>();

        for(ListingEntity listing : userListings){
            allOffers.addAll(listing.getOffers());
        }
        return allOffers.stream().map(mapper::toDomain).toList();
    }
    //принять
    public OfferDto acceptOffer(Long offerId){
        TradeOffer offer = repository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found with id " + offerId));

        UserEntity userEntity = userRepository.findByEmail(UserEmail());
        UserEntity seller = offer.getListing().getSeller();

        if(!seller.getId().equals(userEntity.getId())){
            throw new AccessDeniedException("Offer the seller can accept this offer");
        }

        if(offer.getStatus() != TradeOfferStatus.PENDING){
            throw new IllegalStateException("Offer is not in PENDING status. Current status: " + offer.getStatus());
        }
        ListingEntity listing = offer.getListing();
        if(listing.getStatus() != ListingStatus.ACTIVE){
            throw new IllegalStateException("Listing is not active. Current status: " + listing.getStatus());
        }
        UserEntity buyer = offer.getBuyer();
        DogEntity dogFromListing = listing.getDog();
        DogEntity offeredDog = offer.getOfferedDog();
        BigDecimal offeredMoney = offer.getOffer();

        boolean hasDog = offeredDog != null;
        boolean hasMoney = offeredMoney != null && offeredMoney.compareTo(BigDecimal.ZERO) > 0;

        if(hasMoney){
            if(buyer.getBalance().compareTo(offeredMoney) < 0){
                throw new IllegalStateException("Buyer has insufficient funds. Balance: " +
                        buyer.getBalance() + ", required: " + offeredMoney);
            }
            buyer.setBalance(buyer.getBalance().subtract(offeredMoney));
            seller.setBalance(seller.getBalance().add(offeredMoney));

            userRepository.save(buyer);
            userRepository.save(seller);
        }
        if(hasDog){
            dogFromListing.setUser(buyer);
            offeredDog.setUser(seller);

            dogRepository.save(dogFromListing);
            dogRepository.save(offeredDog);
        }else{
            dogFromListing.setUser(buyer);
            dogRepository.save(dogFromListing);
        }
        listing.setStatus(ListingStatus.SOLD);
        listingRepository.save(listing);

        offer.setStatus(TradeOfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());

        List<TradeOffer> pendingOffers = repository.findByListingIdAndStatus(
                listing.getId(),TradeOfferStatus.PENDING);

        for(TradeOffer pendingOffer : pendingOffers){
            if(!pendingOffer.getId().equals(offer.getId())){
                pendingOffer.setStatus(TradeOfferStatus.CANCELLED);
                pendingOffer.setRespondedAt(LocalDateTime.now());
                repository.save(pendingOffer);
            }
        }
        TradeOffer saved = repository.save(offer);

        return mapper.toDomain(saved);
    }
    //отклонить
    public OfferDto rejectOffer(Long offerId){
        TradeOffer entity = repository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));
        UserEntity userEntity = userRepository.findByEmail(UserEmail());
        ListingEntity listing = entity.getListing();
        if(!userEntity.getId().equals(listing.getSeller().getId())){
            throw new AccessDeniedException("the user is not the owner of the ad");
        }
        if(entity.getStatus() == TradeOfferStatus.PENDING){
            entity.setStatus(TradeOfferStatus.REJECTED);
            entity.setRespondedAt(LocalDateTime.now());
        }
        repository.save(entity);
        return mapper.toDomain(entity);
    }
    //Отмена оффера
    public OfferDto cancelOffer(Long offerId){
        TradeOffer entity = repository.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));
        UserEntity userEntity = userRepository.findByEmail(UserEmail());

        if(!userEntity.getId().equals(entity.getBuyer().getId())){
            throw new AccessDeniedException("the user is not the owner of the ad");
        }
        if(entity.getStatus() == TradeOfferStatus.PENDING){
            entity.setStatus(TradeOfferStatus.CANCELLED);
            entity.setRespondedAt(LocalDateTime.now());
        }
        repository.save(entity);
        return mapper.toDomain(entity);
    }
    // 9. Контрпредложение
    public OfferDto counterOffer(Long originalOfferId, OfferDto counterOfferDto) {
        TradeOffer originalOffer = repository.findById(originalOfferId)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

        UserEntity userEntity = userRepository.findByEmail(UserEmail());

        UserEntity seller = originalOffer.getListing().getSeller();
        if (!seller.getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("Only the seller can make a counter offer");
        }
        if (originalOffer.getStatus() != TradeOfferStatus.PENDING) {
            throw new IllegalStateException("Original offer is not in PENDING status");
        }
        ListingEntity listing = originalOffer.getListing();
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is not active");
        }
        boolean hasDog = counterOfferDto.dogId() != null;
        boolean hasMoney = counterOfferDto.offer() != null && counterOfferDto.offer().compareTo(BigDecimal.ZERO) > 0;

        if (!hasDog && !hasMoney) {
            throw new IllegalArgumentException("Counter offer must include a dog or money");
        }
        DogEntity offeredDog = null;
        if (hasDog) {
            offeredDog = dogRepository.findById(counterOfferDto.dogId())
                    .orElseThrow(() -> new EntityNotFoundException("Dog not found"));
            if(!offeredDog.getUser().getId().equals(seller.getId())){
                throw new AccessDeniedException("You can only offer your own dogs");
            }
            if (offeredDog.getListing() != null && offeredDog.getListing().getStatus() == ListingStatus.ACTIVE) {
                throw new IllegalStateException("Cannot offer a dog that is already listed for sale");
            }
        }
        TradeOffer counterOffer = new TradeOffer();
        counterOffer.setBuyer(seller);
        counterOffer.setListing(listing);
        counterOffer.setOfferedDog(offeredDog);
        counterOffer.setOffer(counterOfferDto.offer());
        counterOffer.setMessage(counterOfferDto.message());
        counterOffer.setStatus(TradeOfferStatus.PENDING);
        counterOffer.setCreatedAt(LocalDateTime.now());
        counterOffer.setRespondedAt(null);

        originalOffer.setStatus(TradeOfferStatus.COUNTER_OFFER);
        originalOffer.setRespondedAt(LocalDateTime.now());

        repository.save(originalOffer);
        TradeOffer saved = repository.save(counterOffer);

        return mapper.toDomain(saved);
    }
}
