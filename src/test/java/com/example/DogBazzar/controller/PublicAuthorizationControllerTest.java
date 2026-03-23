package com.example.DogBazzar.controller;

import com.example.DogBazzar.User.User;
import com.example.DogBazzar.User.UserService;
import com.example.DogBazzar.nosecured.PublicAuthorizationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PublicAuthorizationControllerTest {
   private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PublicAuthorizationController controller;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        SecurityContextHolder.clearContext();
    }
    @Test
    void registerSuccess() throws Exception {
        String name = "Иван Петров";
        String email = "ivan@example.com";
        String password = "password123";

        User userRequest = new User(null,name,email,password,null);

        when(passwordEncoder.encode(password)).thenReturn("encodedPassword123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)  // Отправляем JSON
                        .content(objectMapper.writeValueAsString(userRequest)))  // Тело запроса

                // 3. ПРОВЕРКА (Then)
                .andExpect(status().isCreated())  // Статус 201
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.email").value(email));

        // Проверяем, что методы вызывались
        verify(passwordEncoder).encode(password);  // Пароль должен быть зашифрован
        verify(userService).createUser(any(User.class));  // Пользователь должен быть создан
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }


        /**
         * ТЕСТ 2: Регистрация с пустыми данными
         *
         * Что проверяем:
         * - Если не передать обязательные поля, вернется ошибка 400
         */
        @Test
        void registerWithEmptyData() throws Exception {
            // 1. ПОДГОТОВКА
            User userRequest = new User(null, null, null, null, null);

            // 2. ДЕЙСТВИЕ И ПРОВЕРКА
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))

                    .andExpect(status().isBadRequest());  // Ожидаем ошибку 400

            // Проверяем, что сервис НЕ вызывался
            verify(passwordEncoder, never()).encode(anyString());
            verify(userService, never()).createUser(any());
        }

        /**
         * ТЕСТ 3: Успешный вход
         *
         * Что проверяем:
         * - Если email и пароль верные, вход успешен
         * - Возвращается статус 200
         */
        @Test
        void loginSuccess() throws Exception {
            // 1. ПОДГОТОВКА
            String email = "ivan@example.com";
            String password = "password123";

            Map<String, String> loginRequest = Map.of(
                    "email", email,
                    "password", password
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mock(Authentication.class));

            // 2. ДЕЙСТВИЕ И ПРОВЕРКА
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))

                    .andExpect(status().isOk())  // Статус 200
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.email").value(email));
        }

        /**
         * ТЕСТ 4: Вход с неверным паролем
         *
         * Что проверяем:
         * - Если пароль неверный, возвращается 401 Unauthorized
         */
        @Test
        void loginWithWrongPassword() throws Exception {
            // 1. ПОДГОТОВКА
            Map<String, String> loginRequest = Map.of(
                    "email", "ivan@example.com",
                    "password", "wrongPassword"
            );

            // Настраиваем мок на выброс исключения (как будто пароль неверный)
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // 2. ДЕЙСТВИЕ И ПРОВЕРКА
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))

                    .andExpect(status().isUnauthorized())  // Статус 401
                    .andExpect(jsonPath("$.error").value("Invalid email or password"));
        }

        /**
         * ТЕСТ 5: Вход без email
         *
         * Что проверяем:
         * - Если не передать email, вернется ошибка 400
         */
        @Test
        void loginWithoutEmail() throws Exception {
            // 1. ПОДГОТОВКА
            Map<String, String> loginRequest = Map.of("password", "password123");

            // 2. ДЕЙСТВИЕ И ПРОВЕРКА
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))

                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email and password are required"));
        }

        /**
         * ТЕСТ 6: Успешный выход
         *
         * Что проверяем:
         * - При выходе всегда возвращается 200
         */
        @Test
        void logoutSuccess() throws Exception {
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
    }




