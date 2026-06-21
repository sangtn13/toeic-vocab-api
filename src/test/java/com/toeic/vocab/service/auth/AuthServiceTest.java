package com.toeic.vocab.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.mapper.AuthMapper;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.repository.user.AppUserRepository;
import com.toeic.vocab.request.auth.LoginRequest;
import com.toeic.vocab.request.auth.RegisterRequest;
import com.toeic.vocab.security.auth.AppUserDetails;
import com.toeic.vocab.security.auth.CurrentUserProvider;
import com.toeic.vocab.security.auth.JwtUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Test
    void shouldRegisterUserAndIssueAccessToken() {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(appUserRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(jwtUtils.generateTokenForUser(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtUtils.getExpirationFromJwtToken("jwt-token")).thenReturn(Date.from(Instant.parse("2026-06-07T02:00:00Z")));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        AuthService service = new AuthServiceImpl(
            appUserRepository,
            passwordEncoder,
            currentUserProvider,
            authenticationManager,
            jwtUtils,
            Mappers.getMapper(AuthMapper.class)
        );

        var result = service.register(new RegisterRequest("User@Example.com", "secret123", "Toeic User"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresAt()).isNotNull();
        assertThat(result.user().id()).isEqualTo(userId);
        assertThat(result.user().email()).isEqualTo("user@example.com");
        assertThat(result.user().fullName()).isEqualTo("Toeic User");
        assertThat(result.user().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldLoginUsingAuthenticatedPrincipal() {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        AppUserDetails principal = new AppUserDetails(
                userId,
                "user@example.com",
                "encoded-password",
                "Toeic User",
                UserRole.USER,
                true,
                LocalDateTime.parse("2026-06-07T09:00:00"),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtUtils.generateTokenForUser(authentication)).thenReturn("jwt-token");
        when(jwtUtils.getExpirationFromJwtToken("jwt-token"))
                .thenReturn(Date.from(Instant.parse("2026-06-07T02:00:00Z")));

        AuthService service = new AuthServiceImpl(
                appUserRepository,
                passwordEncoder,
                currentUserProvider,
                authenticationManager,
                jwtUtils,
                Mappers.getMapper(AuthMapper.class));

        var result = service.login(new LoginRequest("User@Example.com", "secret123"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.user().id()).isEqualTo(userId);
        assertThat(result.user().email()).isEqualTo("user@example.com");
        assertThat(result.user().fullName()).isEqualTo("Toeic User");
        assertThat(result.user().role()).isEqualTo(UserRole.USER);
    }
}
