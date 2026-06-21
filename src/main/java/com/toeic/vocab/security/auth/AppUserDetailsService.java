package com.toeic.vocab.security.auth;

import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.repository.user.AppUserRepository;
import java.util.UUID;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = Optional.ofNullable(appUserRepository.findByEmailIgnoreCase(email).orElse(null))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return AppUserDetails.from(user);
    }

    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return AppUserDetails.from(user);
    }
}
