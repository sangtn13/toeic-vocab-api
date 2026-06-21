package com.toeic.vocab.security.auth;

import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.model.user.AppUser;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class AppUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String fullName;
    private final UserRole role;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final Collection<? extends GrantedAuthority> authorities;

    public static AppUserDetails from(AppUser user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new AppUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive()),
                user.getCreatedAt(),
                authorities);
    }

    public AppUser toAppUser() {
        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(password)
                .fullName(fullName)
                .role(role)
                .active(active)
                .build();
        user.setId(id);
        user.setCreatedAt(createdAt);
        return user;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
