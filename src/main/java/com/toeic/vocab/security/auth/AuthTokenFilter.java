package com.toeic.vocab.security.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AppUserDetailsService userDetailsService;
    private final BearerTokenResolver bearerTokenResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = bearerTokenResolver.resolve(request.getHeader(bearerTokenResolver.authorizationHeaderName()));
            if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {
                UUID userId = UUID.fromString(jwtUtils.getUserIdFromJwtToken(jwt));
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | AuthenticationException | IllegalArgumentException exception) {
            // Public endpoints remain accessible even if the client sends a stale, malformed,
            // or no-longer-resolvable bearer token.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
