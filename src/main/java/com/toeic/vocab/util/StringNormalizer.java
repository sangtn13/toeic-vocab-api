package com.toeic.vocab.util;

import java.util.Locale;
import org.springframework.util.StringUtils;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public static String normalizeEmail(String email) {
        String normalizedEmail = trimToNull(email);
        return normalizedEmail == null ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }
}
