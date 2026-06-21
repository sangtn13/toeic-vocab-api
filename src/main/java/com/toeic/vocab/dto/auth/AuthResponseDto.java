package com.toeic.vocab.dto.auth;

import java.time.LocalDateTime;

public record AuthResponseDto(
        String accessToken,
        LocalDateTime expiresAt,
        AuthUserDto user) {
}
