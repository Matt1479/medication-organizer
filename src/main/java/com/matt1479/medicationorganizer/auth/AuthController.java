package com.matt1479.medicationorganizer.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matt1479.medicationorganizer.auth.dto.LoginRequest;
import com.matt1479.medicationorganizer.auth.dto.RegisterRequest;
import com.matt1479.medicationorganizer.auth.dto.TokenResponse;
import com.matt1479.medicationorganizer.users.UserService;
import com.matt1479.medicationorganizer.users.dto.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        String accessToken = tokenService.generateToken(authentication);
        return new TokenResponse(accessToken, tokenService.getTtlSeconds());
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return userService.createUser(
                request.email(),
                request.displayName(),
                request.password());
    }

}
