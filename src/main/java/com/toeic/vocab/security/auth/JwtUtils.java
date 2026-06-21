package com.toeic.vocab.security.auth;

import com.toeic.vocab.model.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    @Value("${toeic.app.jwtSecret}")
    private String jwtSecret;

    @Value("${toeic.app.jwtExpirationMs}")
    private int expirationTime;

    public String generateTokenForUser(Authentication authentication) {
        AppUserDetails userPrincipal = (AppUserDetails) authentication.getPrincipal();
        return buildToken(userPrincipal.getUsername(), String.valueOf(userPrincipal.getId()),
                userPrincipal.getAuthorities());
    }

    public String generateTokenForUser(AppUser user) {
        return buildToken(user.getEmail(), String.valueOf(user.getId()), AppUserDetails.from(user).getAuthorities());
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        return parseClaims(token).get("id", String.class);
    }

    public Date getExpirationFromJwtToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (Exception exception) {
            throw new JwtException("Invalid JWT token: " + exception.getMessage());
        }
    }

    private String buildToken(String email, String userId, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationTime))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
