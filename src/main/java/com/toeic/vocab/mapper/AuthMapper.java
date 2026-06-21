package com.toeic.vocab.mapper;

import com.toeic.vocab.dto.auth.AuthResponseDto;
import com.toeic.vocab.dto.auth.AuthUserDto;
import com.toeic.vocab.model.user.AppUser;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthUserDto toAuthUserDto(AppUser user);

    default AuthResponseDto toAuthResponse(AppUser user, String accessToken, LocalDateTime expiresAt) {
        return new AuthResponseDto(accessToken, expiresAt, toAuthUserDto(user));
    }
}
