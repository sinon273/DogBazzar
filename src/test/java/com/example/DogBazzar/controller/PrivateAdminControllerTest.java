package com.example.DogBazzar.controller;


import com.example.DogBazzar.Dog.Dog;
import com.example.DogBazzar.Dog.DogService;
import com.example.DogBazzar.Dog.Rarity;
import com.example.DogBazzar.Listing.Listing;
import com.example.DogBazzar.Listing.ListingService;
import com.example.DogBazzar.Listing.ListingStatus;
import com.example.DogBazzar.User.User;
import com.example.DogBazzar.User.UserRole;
import com.example.DogBazzar.User.UserService;
import com.example.DogBazzar.secured.PrivateAdminController;
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
class PrivateAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DogService dogService;

    @Mock
    private UserService userService;

    @Mock
    private ListingService listingService;

    @InjectMocks
    private PrivateAdminController controller;

    private ObjectMapper objectMapper;
    private Dog testDog;
    private Dog updatedDog;
    private User testUser;
    private Listing testListing;
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

        // Обновленная собака
        updatedDog = new Dog(
                1L,
                "Обновленная собака",
                "Лабрадор",
                "Черный",
                new BigDecimal("1500.00"),
                "http://example.com/dog2.jpg",
                Rarity.RARE
        );

        // Тестовый пользователь
        testUser = new User(
                1L,
                "Иван Петров",
                "ivan@example.com",
                null,
                UserRole.USER
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
    }

    /**
     * ТЕСТ 1: Получение всех собак (админ)
     * GET /admin/dogs
     */
    @Test
    void getAllDogsSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/dogs"))
                .andExpect(status().isOk())
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
     * ТЕСТ 2: Создание новой собаки (админ)
     * POST /admin/dogs
     */
    @Test
    void createDogSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        Dog newDog = new Dog(
                null,
                "Новая собака",
                "Хаски",
                "Серый",
                new BigDecimal("2000.00"),
                "http://example.com/newdog.jpg",
                Rarity.EPIC
        );

        Dog createdDog = new Dog(
                2L,
                "Новая собака",
                "Хаски",
                "Серый",
                new BigDecimal("2000.00"),
                "http://example.com/newdog.jpg",
                Rarity.EPIC
        );

        when(dogService.createDog(any(Dog.class))).thenReturn(createdDog);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(post("/admin/dogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDog)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Новая собака"))
                .andExpect(jsonPath("$.breed").value("Хаски"))
                .andExpect(jsonPath("$.color").value("Серый"))
                .andExpect(jsonPath("$.price").value(2000.0))
                .andExpect(jsonPath("$.rarity").value("EPIC"));

        verify(dogService).createDog(any(Dog.class));
    }

    /**
     * ТЕСТ 3: Обновление собаки (админ)
     * PUT /admin/dogs/{id}
     */
    @Test
    void updateDogSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        when(dogService.updateDog(eq(1L), any(Dog.class))).thenReturn(updatedDog);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(put("/admin/dogs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Обновленная собака"))
                .andExpect(jsonPath("$.breed").value("Лабрадор"))
                .andExpect(jsonPath("$.color").value("Черный"))
                .andExpect(jsonPath("$.price").value(1500.0))
                .andExpect(jsonPath("$.rarity").value("RARE"));

        verify(dogService).updateDog(eq(1L), any(Dog.class));
    }

    /**
     * ТЕСТ 4: Удаление собаки (админ)
     * DELETE /admin/dogs/{id}
     */
    @Test
    void deleteDogSuccess() throws Exception {
        // 1. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(delete("/admin/dogs/1"))
                .andExpect(status().isNoContent());

        verify(dogService).deleteDog(1L);
    }

    /**
     * ТЕСТ 5: Получение всех пользователей (админ)
     * GET /admin/users
     */
    @Test
    void getAllUsersSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        List<User> users = Arrays.asList(testUser);
        when(userService.findAllUser()).thenReturn(users);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван Петров"))
                .andExpect(jsonPath("$[0].email").value("ivan@example.com"))
                .andExpect(jsonPath("$[0].role").value("USER"));

        verify(userService).findAllUser();
    }

    /**
     * ТЕСТ 6: Получение пользователя по ID (админ)
     * GET /admin/users/{id}
     */
    @Test
    void getUserByIdSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        when(userService.findById(1L)).thenReturn(testUser);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Петров"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).findById(1L);
    }

    /**
     * ТЕСТ 7: Получение пользователя по несуществующему ID
     * GET /admin/users/{id}
     */
    @Test
    void getUserByIdNotFound() throws Exception {
        // 1. ПОДГОТОВКА
        when(userService.findById(999L)).thenThrow(new RuntimeException("User not found"));

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/users/999"))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ТЕСТ 8: Получение всех объявлений (админ)
     * GET /admin/listings
     */
    @Test
    void getAllListingsSuccess() throws Exception {
        // 1. ПОДГОТОВКА
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/listings")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dogId").value(1))
                .andExpect(jsonPath("$[0].price").value(1500.0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].sellerName").value("Продавец Иван"))
                .andExpect(jsonPath("$[0].dogName").value("Тестовая собака"));

        verify(listingService).findAllListing(any());
    }

    /**
     * ТЕСТ 9: Получение всех объявлений без параметров
     * GET /admin/listings
     */
    @Test
    void getAllListingsWithoutParams() throws Exception {
        // 1. ПОДГОТОВКА
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].price").value(1500.0));

        verify(listingService).findAllListing(any());
    }

    /**
     * ТЕСТ 10: Получение всех объявлений с фильтром по цене
     * GET /admin/listings
     */
    @Test
    void getAllListingsWithPriceFilter() throws Exception {
        // 1. ПОДГОТОВКА
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/listings")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1500.0));

        verify(listingService).findAllListing(any());
    }

    /**
     * ТЕСТ 11: Пустой список собак
     */
    @Test
    void getAllDogsEmpty() throws Exception {
        // 1. ПОДГОТОВКА
        when(dogService.searchDogs(any())).thenReturn(List.of());

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/dogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 12: Пустой список пользователей
     */
    @Test
    void getAllUsersEmpty() throws Exception {
        // 1. ПОДГОТОВКА
        when(userService.findAllUser()).thenReturn(List.of());

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 13: Пустой список объявлений
     */
    @Test
    void getAllListingsEmpty() throws Exception {
        // 1. ПОДГОТОВКА
        when(listingService.findAllListing(any())).thenReturn(List.of());

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * ТЕСТ 14: Создание собаки с ошибкой
     */
    @Test
    void createDogWithError() throws Exception {
        // 1. ПОДГОТОВКА
        Dog newDog = new Dog(
                null,
                "Новая собака",
                "Хаски",
                "Серый",
                new BigDecimal("2000.00"),
                "http://example.com/newdog.jpg",
                Rarity.EPIC
        );

        when(dogService.createDog(any(Dog.class)))
                .thenThrow(new RuntimeException("Ошибка создания"));

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(post("/admin/dogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDog)))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ТЕСТ 15: Обновление несуществующей собаки
     */
    @Test
    void updateDogNotFound() throws Exception {
        // 1. ПОДГОТОВКА
        when(dogService.updateDog(eq(999L), any(Dog.class)))
                .thenThrow(new RuntimeException("Dog not found"));

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(put("/admin/dogs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDog)))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ТЕСТ 16: Удаление несуществующей собаки
     */
    @Test
    void deleteDogNotFound() throws Exception {
        // 1. ПОДГОТОВКА
        doThrow(new RuntimeException("Dog not found"))
                .when(dogService).deleteDog(999L);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(delete("/admin/dogs/999"))
                .andExpect(status().is5xxServerError());
    }

    /**
     * ТЕСТ 17: Пагинация для списка собак
     */
    @Test
    void getAllDogsWithPagination() throws Exception {
        // 1. ПОДГОТОВКА
        List<Dog> dogs = Arrays.asList(testDog);
        when(dogService.searchDogs(any())).thenReturn(dogs);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/dogs")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    /**
     * ТЕСТ 18: Пагинация для списка объявлений
     */
    @Test
    void getAllListingsWithPagination() throws Exception {
        // 1. ПОДГОТОВКА
        List<Listing> listings = Arrays.asList(testListing);
        when(listingService.findAllListing(any())).thenReturn(listings);

        // 2. ДЕЙСТВИЕ И ПРОВЕРКА
        mockMvc.perform(get("/admin/listings")
                        .param("page", "2")
                        .param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
