package com.example.DogBazzar.nosecured;

import com.example.DogBazzar.User.User;
import com.example.DogBazzar.User.UserRole;
import com.example.DogBazzar.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


//вход
//выход
@RestController
@RequestMapping("/auth")
public class PublicAuthorizationController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PublicAuthorizationController(AuthenticationManager authenticationManager,UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){

        if(user.name() == null || user.email() == null || user.password() == null){
            return ResponseEntity.badRequest().build();
        }
        String encodedPassword = passwordEncoder.encode(user.password());
        User newUser = new User(
                null,
                user.name(),
                user.email(),
                encodedPassword,
                UserRole.USER
        );
        userService.createUser(newUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        newUser.email(),
                        user.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message","User registered successfully",
                        "email",user.email()
                ));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> payload){
        try{
            String email = payload.get("email");
            String password = payload.get("password");

            if(email == null || password == null){
                return ResponseEntity.badRequest()
                        .body(Map.of("error","Email and password are required"));
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email,password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message","Login successful",
                            "email",email
                    ));
        }catch(BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","Invalid email or password"));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        SecurityContextHolder.clearContext();
        return  ResponseEntity.ok()
                .body(Map.of("message","Logged out successfully"));
    }
}
