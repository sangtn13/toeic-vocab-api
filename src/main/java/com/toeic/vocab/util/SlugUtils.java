package com.toeic.vocab.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input == null ? "" : input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String slug = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "study-set" : slug;
    }
}
