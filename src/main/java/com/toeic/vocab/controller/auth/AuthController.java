package com.toeic.vocab.controller.auth;

import com.toeic.vocab.dto.auth.AuthResponseDto;
import com.toeic.vocab.dto.auth.AuthUserDto;
import com.toeic.vocab.request.auth.LoginRequest;
import com.toeic.vocab.request.auth.RegisterRequest;
import com.toeic.vocab.response.ApiResponse;
import com.toeic.vocab.security.auth.BearerTokenResolver;
import com.toeic.vocab.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/api/v1}/auth")
public class AuthController {

    private final AuthService authService;
    private final BearerTokenResolver bearerTokenResolver;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserDto>> getCurrentUser() {
        return ResponseEntity
                .ok(ApiResponse.success("Current user fetched successfully", authService.getCurrentUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        authService.logout(bearerTokenResolver.resolve(authorizationHeader));
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
