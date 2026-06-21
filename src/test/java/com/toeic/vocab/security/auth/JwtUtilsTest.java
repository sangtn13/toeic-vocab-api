package com.toeic.vocab.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.model.user.AppUser;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "g0qlJwfjNUHoDn4YOos9jItP5/srQ3QXbPwJjzQFfyTTKpVH+NRLFSGgErlYp3KnThZ+tXBmHms5ysdmk8WL6g==");
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 3600000);
    }

    @Test
    void shouldGenerateAndValidateJwtForUser() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AppUser user = AppUser.builder()
            .email("admin@test.com")
            .passwordHash("encoded")
            .fullName("Admin")
            .role(UserRole.ADMIN)
            .active(true)
            .build();
        user.setId(userId);

        String token = jwtUtils.generateTokenForUser(user);

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("admin@test.com");
        assertThat(jwtUtils.getUserIdFromJwtToken(token)).isEqualTo(userId.toString());
        assertThat(jwtUtils.getExpirationFromJwtToken(token)).isNotNull();
    }
}
