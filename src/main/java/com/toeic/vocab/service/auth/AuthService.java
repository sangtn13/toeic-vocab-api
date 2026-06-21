package com.toeic.vocab.service.auth;

import com.toeic.vocab.dto.auth.AuthResponseDto;
import com.toeic.vocab.dto.auth.AuthUserDto;
import com.toeic.vocab.request.auth.LoginRequest;
import com.toeic.vocab.request.auth.RegisterRequest;

public interface AuthService {

    AuthResponseDto register(RegisterRequest request);

    AuthResponseDto login(LoginRequest request);

    AuthUserDto getCurrentUser();

    void logout(String accessToken);
}
