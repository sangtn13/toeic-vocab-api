package com.toeic.vocab.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AppUserDetailsService userDetailsService;

    @Mock
    private BearerTokenResolver bearerTokenResolver;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldIgnoreInvalidBearerTokenAndContinueFilterChain() throws Exception {
        AuthTokenFilter filter = new AuthTokenFilter(
                jwtUtils,
                userDetailsService,
                bearerTokenResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/study-sets");
        request.addHeader("Authorization", "Bearer not-a-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        FilterChain chain = new TrackingFilterChain(chainInvoked);

        when(bearerTokenResolver.authorizationHeaderName()).thenReturn("Authorization");
        when(bearerTokenResolver.resolve("Bearer not-a-jwt")).thenReturn("not-a-jwt");
        when(jwtUtils.validateJwtToken("not-a-jwt"))
                .thenThrow(new io.jsonwebtoken.JwtException("Invalid JWT token: malformed"));

        filter.doFilter(request, response, chain);

        assertThat(chainInvoked.get()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldIgnoreBearerTokenWhenUserNoLongerExistsAndContinueFilterChain() throws Exception {
        AuthTokenFilter filter = new AuthTokenFilter(
                jwtUtils,
                userDetailsService,
                bearerTokenResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/study-sets");
        request.addHeader("Authorization", "Bearer valid-jwt-for-deleted-user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        FilterChain chain = new TrackingFilterChain(chainInvoked);

        when(bearerTokenResolver.authorizationHeaderName()).thenReturn("Authorization");
        when(bearerTokenResolver.resolve("Bearer valid-jwt-for-deleted-user")).thenReturn("valid-jwt-for-deleted-user");
        when(jwtUtils.validateJwtToken("valid-jwt-for-deleted-user")).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken("valid-jwt-for-deleted-user"))
                .thenReturn("11111111-1111-1111-1111-111111111111");
        when(userDetailsService.loadUserById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenThrow(new UsernameNotFoundException("User not found"));

        filter.doFilter(request, response, chain);

        assertThat(chainInvoked.get()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static final class TrackingFilterChain implements FilterChain {

        private final AtomicBoolean chainInvoked;

        private TrackingFilterChain(AtomicBoolean chainInvoked) {
            this.chainInvoked = chainInvoked;
        }

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                throws IOException {
            chainInvoked.set(true);
        }
    }
}
