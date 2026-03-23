package com.example.DogBazzar.controller;

import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Dog.Rarity;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.Listing.ListingService;
import com.example.DogBazzar.Listing.ListingStatus;
import com.example.DogBazzar.nosecured.PublicHomeController;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicHomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DogService dogService;

    @Mock
    private ListingService listingService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PublicHomeController controller;

    private ObjectMapper objectMapper;
    private Dog testDog;
    private Listing testListing;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
        now = LocalDateTime.now();

        // ИСПРАВЛЕНО: Создаем тестовую собаку с правильными полями
        testDog = new Dog(
                1L,                    // id
                "Тестовая собака",      // name
                "Овчарка",             // breed
                "Коричневый",          // color
                new BigDecimal("1000.00"),  // price
                "http://example.com/dog.jpg",  // imageUrl
                Rarity.COMMON           // rarity
        );

        // ИСПРАВЛЕНО: Создаем тестовое объявление с правильными полями из вашего record
        testListing = new Listing(
                1L,                    // id
                1L,                    // dogId
                new BigDecimal("1500.00"),  // price
                ListingStatus.ACTIVE,   // status
                now,                   // createdAt
                "Продавец Иван",       // sellerName
                "Тестовая собака"       // dogName
        );
    }

    /**
     * ТЕСТ 1: Получение списка собак с фильтрацией
     */
    @Test
    void getShowcaseSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/showcase")
                        .param("breed", "Овчарка")
                        .param("rarity", "COMMON")
                        .param("minPrice", "500")
                        .param("maxPrice", "2000")
                        .param("page", "0")
                        .param("size", "20"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Проверяем все поля Dog
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Тестовая собака"))
                .andExpect(jsonPath("$[0].breed").value("Овчарка"))
                .andExpect(jsonPath("$[0].color").value("Коричневый"))
                .andExpect(jsonPath("$[0].price").value(1000.0))
                .andExpect(jsonPath("$[0].imageUrl").value("http://example.com/dog.jpg"))
                .andExpect(jsonPath("$[0].rarity").value("COMMON"));

        verify(dogService).searchDogs(any());
    }

    /**
     * ТЕСТ 2: Получение собаки по ID (успешно)
     */
    @Test
    void getDogByIdSuccess() throws Exception {
        when(dogService.getDogId(1L)).thenReturn(testDog);

        mockMvc.perform(get("/showcase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тестовая собака"))
                .andExpect(jsonPath("$.breed").value("Овчарка"))
                .andExpect(jsonPath("$.color").value("Коричневый"))
                .andExpect(jsonPath("$.price").value(1000.0))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/dog.jpg"))
                .andExpect(jsonPath("$.rarity").value("COMMON"));
    }

    /**
     * ТЕСТ 3: Получение собаки по несуществующему ID
     */
    @Test
    void getDogByIdNotFound() throws Exception {
        when(dogService.getDogId(999L)).thenReturn(null);
        mockMvc.perform(get("/showcase/999"))
                .andExpect(status().isNotFound());
    }

    /**
     * ТЕСТ 4: Покупка собаки (авторизованный пользователь)
     */
    @Test
    void buyDogAuthorizedSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(listingService.buyListing(1L)).thenReturn(testListing);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/showcase/1/buy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dog purchased successfully"))
                .andExpect(jsonPath("$.listingId").value(1))
                .andExpect(jsonPath("$.price").value(1500.0));

        verify(listingService).buyListing(1L);
    }

    /**
     * ТЕСТ 5: Покупка собаки (НЕавторизованный пользователь)
     */
    @Test
    void buyDogUnauthorized() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/showcase/1/buy"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"))
                .andExpect(jsonPath("$.message").value("Please login to buy a dog"));

        verify(listingService, never()).buyListing(any());
    }

    /**
     * ТЕСТ 6: Покупка собаки (анонимный пользователь)
     */
    @Test
    void buyDogAnonymousUser() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/showcase/1/buy"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));

        verify(listingService, never()).buyListing(any());
    }

    /**
     * ТЕСТ 7: Покупка собаки (ошибка - собака уже продана)
     */
    @Test
    void buyDogAlreadySold() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(listingService.buyListing(1L))
                .thenThrow(new IllegalStateException("Listing is not active"));

        mockMvc.perform(get("/showcase/1/buy"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Listing is not active"));
    }

    /**
     * ТЕСТ 8: Покупка собаки (объявление не найдено)
     */
    @Test
    void buyDogListingNotFound() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(listingService.buyListing(999L))
                .thenThrow(new EntityNotFoundException("Listing not found"));

        mockMvc.perform(get("/showcase/999/buy"))
                .andExpect(status().isNotFound());
    }

    /**
     * ТЕСТ 9: Получение списка активных объявлений
     */
    @Test
    void getActiveListingsSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/showcase/listings")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("page", "0")
                        .param("size", "20"))

                .andExpect(status().isOk())
                // Проверяем все поля Listing
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dogId").value(1))
                .andExpect(jsonPath("$[0].price").value(1500.0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].sellerName").value("Продавец Иван"))
                .andExpect(jsonPath("$[0].dogName").value("Тестовая собака"));

        verify(listingService).findAllListing(any());
    }

    /**
     * ТЕСТ 10: Получение списка активных объявлений без параметров
     */
    @Test
    void getActiveListingsWithoutParams() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        mockMvc.perform(get("/showcase/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dogId").value(1));
    }

    /**
     * ТЕСТ 11: Пустой список собак
     */
    @Test
    void getShowcaseEmptyList() throws Exception {
        when(dogService.searchDogs(any())).thenReturn(List.of());

        mockMvc.perform(get("/showcase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 12: Пустой список объявлений
     */
    @Test
    void getActiveListingsEmpty() throws Exception {
        when(listingService.findAllListing(any())).thenReturn(List.of());

        mockMvc.perform(get("/showcase/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 13: Фильтрация собак по цене
     */
    @Test
    void getShowcaseWithPriceFilter() throws Exception {
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        mockMvc.perform(get("/showcase")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1000.0));
    }

    /**
     * ТЕСТ 14: Фильтрация собак по редкости
     */
    @Test
    void getShowcaseWithRarityFilter() throws Exception {
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        mockMvc.perform(get("/showcase")
                        .param("rarity", "COMMON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rarity").value("COMMON"));
    }

    /**
     * ТЕСТ 15: Фильтрация объявлений по цене
     */
    @Test
    void getActiveListingsWithPriceFilter() throws Exception {
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        mockMvc.perform(get("/showcase/listings")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1500.0));
    }

    /**
     * ТЕСТ 16: Проверка пагинации
     */
    @Test
    void getShowcaseWithPagination() throws Exception {
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        mockMvc.perform(get("/showcase")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

