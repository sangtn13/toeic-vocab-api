package com.toeic.vocab.security.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BearerTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    public String resolve(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    public String authorizationHeaderName() {
        return HttpHeaders.AUTHORIZATION;
    }
}
