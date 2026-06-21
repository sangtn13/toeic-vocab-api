package com.toeic.vocab.dto.auth;

import com.toeic.vocab.enums.UserRole;
import java.util.UUID;

public record AuthUserDto(
        UUID id,
        String email,
        String fullName,
        UserRole role) {
}
