package com.toeic.vocab.util;

import java.util.regex.Pattern;

public final class StudyTextUtils {

    private StudyTextUtils() {
    }

    public static String normalizeAnswer(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public static String maskWord(String sentence, String answer) {
        if (sentence == null || sentence.isBlank() || answer == null || answer.isBlank()) {
            return sentence;
        }

        String[] answerParts = answer.trim().split("\\s+");
        String masked = sentence;
        for (String part : answerParts) {
            if (part.isBlank()) {
                continue;
            }
            masked = Pattern.compile("\\b" + Pattern.quote(part) + "\\b", Pattern.CASE_INSENSITIVE)
                    .matcher(masked)
                    .replaceAll("******");
        }
        return masked;
    }
}
