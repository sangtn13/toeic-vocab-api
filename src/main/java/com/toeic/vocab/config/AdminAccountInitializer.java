package com.toeic.vocab.config;

import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.repository.user.AppUserRepository;
import com.toeic.vocab.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.seed-admin-email}")
    private String adminEmail;

    @Value("${app.auth.seed-admin-password}")
    private String adminPassword;

    @Value("${app.auth.seed-admin-name}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            return;
        }

        String normalizedEmail = StringNormalizer.normalizeEmail(adminEmail);
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        AppUser adminUser = AppUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(StringNormalizer.trimToNull(adminPassword)))
                .fullName(StringUtils.hasText(adminName) ? adminName.trim() : "System Admin")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        appUserRepository.save(adminUser);
        log.info("Seeded default admin account for {}", normalizedEmail);
    }
}
