package com.toeic.vocab.security.auth;

import com.toeic.vocab.exception.auth.UnauthorizedException;
import com.toeic.vocab.model.user.AppUser;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserProvider {

    public Optional<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails appUserDetails) {
            return Optional.of(appUserDetails.toAppUser());
        }
        if (principal instanceof UserDetails userDetails) {
            if (userDetails instanceof AppUserDetails appUserDetails) {
                return Optional.of(appUserDetails.toAppUser());
            }
            return Optional.empty();
        }
        String username = authentication.getName();
        if (!StringUtils.hasText(username) || "anonymousUser".equalsIgnoreCase(username)) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public AppUser getRequiredUser() {
        return getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required for this action."));
    }
}
