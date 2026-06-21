package com.toeic.vocab.config;

import com.toeic.vocab.security.auth.CurrentUserProvider;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.util.StringUtils;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware(ObjectProvider<CurrentUserProvider> currentUserProviderProvider) {
        return () -> Optional.ofNullable(currentUserProviderProvider.getIfAvailable())
                .flatMap(CurrentUserProvider::getCurrentUser)
                .map(user -> user.getEmail())
                .filter(StringUtils::hasText)
                .or(() -> Optional.of("system"));
    }
}
