package com.toeic.vocab.service.auth;

import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.exception.auth.UnauthorizedException;
import com.toeic.vocab.mapper.AuthMapper;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.repository.user.AppUserRepository;
import com.toeic.vocab.request.auth.LoginRequest;
import com.toeic.vocab.request.auth.RegisterRequest;
import com.toeic.vocab.security.auth.AppUserDetails;
import com.toeic.vocab.security.auth.CurrentUserProvider;
import com.toeic.vocab.security.auth.JwtUtils;
import com.toeic.vocab.util.AppTime;
import com.toeic.vocab.util.StringNormalizer;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public com.toeic.vocab.dto.auth.AuthResponseDto register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        AppUser user = AppUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(UserRole.USER)
                .active(true)
                .build();
        AppUser savedUser = appUserRepository.save(user);
        String accessToken = jwtUtils.generateTokenForUser(savedUser);
        return authMapper.toAuthResponse(savedUser, accessToken, resolveExpiration(accessToken));
    }

    @Override
    @Transactional
    public com.toeic.vocab.dto.auth.AuthResponseDto login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        AppUser user = ((AppUserDetails) authentication.getPrincipal()).toAppUser();
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        String accessToken = jwtUtils.generateTokenForUser(authentication);
        return authMapper.toAuthResponse(user, accessToken, resolveExpiration(accessToken));
    }

    @Override
    @Transactional(readOnly = true)
    public com.toeic.vocab.dto.auth.AuthUserDto getCurrentUser() {
        return authMapper.toAuthUserDto(currentUserProvider.getRequiredUser());
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new UnauthorizedException("Missing bearer token.");
        }
        currentUserProvider.getRequiredUser();
    }

    private LocalDateTime resolveExpiration(String accessToken) {
        return LocalDateTime.ofInstant(
                jwtUtils.getExpirationFromJwtToken(accessToken).toInstant(),
                AppTime.ZONE_ID);
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = StringNormalizer.normalizeEmail(email);
        if (!StringUtils.hasText(normalizedEmail)) {
            throw new IllegalArgumentException("Email is required.");
        }
        return normalizedEmail;
    }
}
