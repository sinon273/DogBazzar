package com.example.DogBazzar.controller;

import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Dog.Rarity;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.Listing.ListingService;
import com.example.DogBazzar.Listing.ListingStatus;
import com.example.DogBazzar.TradeOffer.OfferDto;
import com.example.DogBazzar.TradeOffer.OfferService;
import com.example.DogBazzar.TradeOffer.TradeOfferStatus;
import com.example.DogBazzar.User.User;
import com.example.DogBazzar.User.UserEntity;
import com.example.DogBazzar.User.UserRole;
import com.example.DogBazzar.User.UserService;
import com.example.DogBazzar.secured.PrivateAccountController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrivateAccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DogService dogService;

    @Mock
    private ListingService listingService;

    @Mock
    private OfferService offerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PrivateAccountController controller;

    private ObjectMapper objectMapper;
    private Dog testDog;
    private Listing testListing;
    private OfferDto testOffer;
    private OfferDto testCounterOffer;
    private UserEntity testUserEntity;
    private User testUser;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        now = LocalDateTime.now();

        // Тестовая собака
        testDog = new Dog(
                1L,
                "Тестовая собака",
                "Овчарка",
                "Коричневый",
                new BigDecimal("1000.00"),
                "http://example.com/dog.jpg",
                Rarity.COMMON
        );

        // Тестовое объявление
        testListing = new Listing(
                1L,
                1L,
                new BigDecimal("1500.00"),
                ListingStatus.ACTIVE,
                now,
                "Продавец Иван",
                "Тестовая собака"
        );

        // ИСПРАВЛЕНО: Тестовый оффер со всеми полями
        testOffer = new OfferDto(
                1L,                          // id
                1L,                          // listingId
                2L,                          // buyerId
                "Покупатель Петр",           // buyerName
                1L,                          // sellerId
                "Продавец Иван",             // sellerName
                1L,                          // dogId
                "Тестовая собака",            // dogName
                new BigDecimal("1200.00"),    // offer
                "Хочу купить вашу собаку",    // message
                TradeOfferStatus.PENDING,     // status
                now,                          // createdAt
                null                          // respondedAt
        );

        // ИСПРАВЛЕНО: Тестовое контрпредложение
        testCounterOffer = new OfferDto(
                2L,                          // id
                1L,                          // listingId
                1L,                          // buyerId (теперь продавец становится покупателем)
                "Продавец Иван",              // buyerName
                2L,                          // sellerId (покупатель становится продавцом)
                "Покупатель Петр",            // sellerName
                2L,                          // dogId (другая собака)
                "Другая собака",               // dogName
                new BigDecimal("1300.00"),    // offer
                "Могу предложить 1300",       // message
                TradeOfferStatus.PENDING,     // status
                now,                          // createdAt
                null                          // respondedAt
        );

        // Тестовый пользователь (Entity)
        testUserEntity = new UserEntity();
        testUserEntity.setId(1L);
        testUserEntity.setUsername("Иван Петров");
        testUserEntity.setEmail("ivan@example.com");
        testUserEntity.setRole(UserRole.USER);
        testUserEntity.setBalance(new BigDecimal("5000.00"));

        // Тестовый пользователь (Domain)
        testUser = new User(
                1L,
                "Иван Петров",
                "ivan@example.com",
                null,
                UserRole.USER
        );
    }

    /**
     * ТЕСТ 1: Получение моих собак
     * GET /account/dogs
     */
    @Test
    void getMyDogsSuccess() throws Exception {
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.getMyDogs()).thenReturn(dogs);

        mockMvc.perform(get("/account/dogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Тестовая собака"))
                .andExpect(jsonPath("$[0].breed").value("Овчарка"))
                .andExpect(jsonPath("$[0].color").value("Коричневый"))
                .andExpect(jsonPath("$[0].price").value(1000.0))
                .andExpect(jsonPath("$[0].imageUrl").value("http://example.com/dog.jpg"))
                .andExpect(jsonPath("$[0].rarity").value("COMMON"));

        verify(dogService).getMyDogs();
    }

    /**
     * ТЕСТ 2: Создание объявления о продаже
     * POST /account/listings
     */
    @Test
    void createListingSuccess() throws Exception {
        when(listingService.createListing(any(Listing.class), eq(1L))).thenReturn(testListing);

        mockMvc.perform(post("/account/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testListing)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dogId").value(1))
                .andExpect(jsonPath("$.price").value(1500.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.sellerName").value("Продавец Иван"))
                .andExpect(jsonPath("$.dogName").value("Тестовая собака"));

        verify(listingService).createListing(any(Listing.class), eq(1L));
    }

    /**
     * ТЕСТ 3: Получение моих объявлений
     * GET /account/listings
     */
    @Test
    void getMyListingsSuccess() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.getMyListings()).thenReturn(listings);

        mockMvc.perform(get("/account/listings")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dogId").value(1))
                .andExpect(jsonPath("$[0].price").value(1500.0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(listingService).getMyListings();
    }

    /**
     * ТЕСТ 4: Снятие объявления с продажи
     * DELETE /account/listings/{id}
     */
    @Test
    void cancelListingSuccess() throws Exception {
        mockMvc.perform(delete("/account/listings/1"))
                .andExpect(status().isNoContent());

        verify(listingService).cancellationListing(1L);
    }

    /**
     * ТЕСТ 5: Получение исходящих офферов (мои предложения)
     * GET /account/offers/outgoing
     */
    @Test
    void getOutgoingOffersSuccess() throws Exception {
        List<OfferDto> offers = Arrays.asList(testOffer);
        when(offerService.getMyOffers()).thenReturn(offers);

        mockMvc.perform(get("/account/offers/outgoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].listingId").value(1))
                .andExpect(jsonPath("$[0].buyerId").value(2))
                .andExpect(jsonPath("$[0].buyerName").value("Покупатель Петр"))
                .andExpect(jsonPath("$[0].sellerId").value(1))
                .andExpect(jsonPath("$[0].sellerName").value("Продавец Иван"))
                .andExpect(jsonPath("$[0].dogId").value(1))
                .andExpect(jsonPath("$[0].dogName").value("Тестовая собака"))
                .andExpect(jsonPath("$[0].offer").value(1200.0))
                .andExpect(jsonPath("$[0].message").value("Хочу купить вашу собаку"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(offerService).getMyOffers();
    }

    /**
     * ТЕСТ 6: Получение входящих офферов (предложения мне)
     * GET /account/offers/incoming
     */
    @Test
    void getIncomingOffersSuccess() throws Exception {
        List<OfferDto> offers = Arrays.asList(testOffer);
        when(offerService.getIncomingOffers()).thenReturn(offers);

        mockMvc.perform(get("/account/offers/incoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].offer").value(1200.0))
                .andExpect(jsonPath("$[0].message").value("Хочу купить вашу собаку"));

        verify(offerService).getIncomingOffers();
    }

    /**
     * ТЕСТ 7: Создание оффера
     * POST /account/offers
     */
    @Test
    void createOfferSuccess() throws Exception {
        when(offerService.createOffer(any(OfferDto.class))).thenReturn(testOffer);

        mockMvc.perform(post("/account/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOffer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.listingId").value(1))
                .andExpect(jsonPath("$.offer").value(1200.0))
                .andExpect(jsonPath("$.message").value("Хочу купить вашу собаку"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(offerService).createOffer(any(OfferDto.class));
    }

    /**
     * ТЕСТ 8: Принятие оффера
     * POST /account/offers/{id}/accept
     */
    @Test
    void acceptOfferSuccess() throws Exception {
        OfferDto acceptedOffer = new OfferDto(
                1L, 1L, 2L, "Покупатель Петр", 1L, "Продавец Иван",
                1L, "Тестовая собака", new BigDecimal("1200.00"),
                "Хочу купить вашу собаку", TradeOfferStatus.ACCEPTED, now, now
        );
        when(offerService.acceptOffer(1L)).thenReturn(acceptedOffer);

        mockMvc.perform(post("/account/offers/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(offerService).acceptOffer(1L);
    }

    /**
     * ТЕСТ 9: Отклонение оффера
     * POST /account/offers/{id}/reject
     */
    @Test
    void rejectOfferSuccess() throws Exception {
        OfferDto rejectedOffer = new OfferDto(
                1L, 1L, 2L, "Покупатель Петр", 1L, "Продавец Иван",
                1L, "Тестовая собака", new BigDecimal("1200.00"),
                "Хочу купить вашу собаку", TradeOfferStatus.REJECTED, now, now
        );
        when(offerService.rejectOffer(1L)).thenReturn(rejectedOffer);

        mockMvc.perform(post("/account/offers/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(offerService).rejectOffer(1L);
    }

    /**
     * ТЕСТ 10: Контрпредложение
     * POST /account/offers/{id}/counter
     */
    @Test
    void counterOfferSuccess() throws Exception {
        OfferDto counterRequest = new OfferDto(
                null, 1L, null, null, null, null,
                2L, "Другая собака", new BigDecimal("1300.00"),
                "Могу предложить 1300", null, null, null
        );

        when(offerService.counterOffer(eq(1L), any(OfferDto.class))).thenReturn(testCounterOffer);

        mockMvc.perform(post("/account/offers/1/counter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(counterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.listingId").value(1))
                .andExpect(jsonPath("$.buyerId").value(1))
                .andExpect(jsonPath("$.buyerName").value("Продавец Иван"))
                .andExpect(jsonPath("$.sellerId").value(2))
                .andExpect(jsonPath("$.sellerName").value("Покупатель Петр"))
                .andExpect(jsonPath("$.dogId").value(2))
                .andExpect(jsonPath("$.dogName").value("Другая собака"))
                .andExpect(jsonPath("$.offer").value(1300.0))
                .andExpect(jsonPath("$.message").value("Могу предложить 1300"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(offerService).counterOffer(eq(1L), any(OfferDto.class));
    }

    /**
     * ТЕСТ 11: Отмена своего оффера
     * DELETE /account/offers/{id}
     */
    @Test
    void cancelOfferSuccess() throws Exception {
        mockMvc.perform(delete("/account/offers/1"))
                .andExpect(status().isNoContent());

        verify(offerService).cancelOffer(1L);
    }

    /**
     * ТЕСТ 12: Получение профиля
     * GET /account/profile
     */
    @Test
    void getProfileSuccess() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUserEntity);

        mockMvc.perform(get("/account/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Петров"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getCurrentUser();
    }

    /**
     * ТЕСТ 13: Обновление профиля
     * PUT /account/profile
     */
    @Test
    void updateProfileSuccess() throws Exception {
        User updatedUser = new User(
                null,
                "Новое Имя",
                "newemail@example.com",
                "newpassword",
                null
        );

        when(userService.getCurrentId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(testUserEntity);

        mockMvc.perform(put("/account/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk());

        verify(userService).UpdateUserId(any(User.class), eq(1L));
    }

    /**
     * ТЕСТ 14: Получение баланса
     * GET /account/balance
     */
    @Test
    void getBalanceSuccess() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUserEntity);

        mockMvc.perform(get("/account/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5000.0));

        verify(userService).getCurrentUser();
    }

    /**
     * ТЕСТ 15: Пополнение баланса
     * POST /account/balance
     */
    @Test
    void addFundsSuccess() throws Exception {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal newBalance = new BigDecimal("6000.00");

        when(userService.getCurrentId()).thenReturn(1L);
        when(userService.replenishment(1L, amount)).thenReturn(newBalance);

        mockMvc.perform(post("/account/balance")
                        .param("amount", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(6000.0));

        verify(userService).replenishment(1L, amount);
    }

    /**
     * ТЕСТ 16: Получение всех офферов на конкретное объявление
     * GET /account/listings/{listingId}/offers
     */
    @Test
    void getOffersByListingSuccess() throws Exception {
        List<OfferDto> offers = Arrays.asList(testOffer);
        when(offerService.getOffersByListing(1L)).thenReturn(offers);

        mockMvc.perform(get("/account/listings/1/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].listingId").value(1))
                .andExpect(jsonPath("$[0].offer").value(1200.0));

        verify(offerService).getOffersByListing(1L);
    }

    /**
     * ТЕСТ 17: Получение оффера по ID
     * GET /account/offers/{id}
     */
    @Test
    void getOfferByIdSuccess() throws Exception {
        when(offerService.getOfferById(1L)).thenReturn(testOffer);

        mockMvc.perform(get("/account/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.listingId").value(1))
                .andExpect(jsonPath("$.buyerName").value("Покупатель Петр"))
                .andExpect(jsonPath("$.sellerName").value("Продавец Иван"))
                .andExpect(jsonPath("$.offer").value(1200.0))
                .andExpect(jsonPath("$.message").value("Хочу купить вашу собаку"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(offerService).getOfferById(1L);
    }

    /**
     * ТЕСТ 18: Попытка создания объявления с ошибкой
     */
    @Test
    void createListingWithError() throws Exception {
        when(listingService.createListing(any(Listing.class), eq(1L)))
                .thenThrow(new RuntimeException("Ошибка создания"));

        mockMvc.perform(post("/account/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testListing)))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ТЕСТ 19: Пустой список собак
     */
    @Test
    void getMyDogsEmpty() throws Exception {
        when(dogService.getMyDogs()).thenReturn(List.of());

        mockMvc.perform(get("/account/dogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 20: Пустой список офферов
     */
    @Test
    void getOutgoingOffersEmpty() throws Exception {
        when(offerService.getMyOffers()).thenReturn(List.of());

        mockMvc.perform(get("/account/offers/outgoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
