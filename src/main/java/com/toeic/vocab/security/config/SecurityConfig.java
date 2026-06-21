package com.toeic.vocab.security.config;

import com.toeic.vocab.security.auth.AppUserDetailsService;
import com.toeic.vocab.security.auth.AuthTokenFilter;
import com.toeic.vocab.security.auth.JwtAuthEntryPoint;
import com.toeic.vocab.security.auth.RestAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthTokenFilter authTokenFilter,
            AppUserDetailsService appUserDetailsService,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            @Value("${api.prefix:/api/v1}") String apiPrefix) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/actuator/health")
                        .permitAll()
                        .requestMatchers(apiPrefix + "/auth/login", apiPrefix + "/auth/register")
                        .permitAll()
                        .requestMatchers(apiPrefix + "/public/**")
                        .permitAll()
                        .requestMatchers(apiPrefix + "/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers(apiPrefix + "/auth/me", apiPrefix + "/auth/logout")
                        .authenticated()
                        .anyRequest()
                        .authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .userDetailsService(appUserDetailsService)
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
