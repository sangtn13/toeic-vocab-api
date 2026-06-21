package com.toeic.vocab.service.admin;

import com.toeic.vocab.repository.studyset.StudySetRepository;
import com.toeic.vocab.util.SlugUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudySetSlugGenerator {

    private static final int MAX_STUDY_SET_SLUG_LENGTH = 160;

    private final StudySetRepository studySetRepository;

    public String resolveUniqueSlug(String title, UUID currentStudySetId) {
        String baseSlug = normalizeSlugLength(SlugUtils.toSlug(title), MAX_STUDY_SET_SLUG_LENGTH);
        String candidate = baseSlug;
        int suffix = 2;

        while (slugExists(candidate, currentStudySetId)) {
            candidate = appendSlugSuffix(baseSlug, suffix);
            suffix++;
        }

        return candidate;
    }

    private boolean slugExists(String slug, UUID currentStudySetId) {
        return currentStudySetId == null
                ? studySetRepository.existsBySlugIgnoreCase(slug)
                : studySetRepository.existsBySlugIgnoreCaseAndIdNot(slug, currentStudySetId);
    }

    private String appendSlugSuffix(String baseSlug, int suffix) {
        String suffixText = "-" + suffix;
        String truncatedBase = normalizeSlugLength(baseSlug, MAX_STUDY_SET_SLUG_LENGTH - suffixText.length());
        return truncatedBase + suffixText;
    }

    private String normalizeSlugLength(String slug, int maxLength) {
        if (slug.length() <= maxLength) {
            return slug;
        }

        String truncated = slug.substring(0, maxLength).replaceAll("-+$", "");
        if (!truncated.isBlank()) {
            return truncated;
        }

        String fallback = "study-set";
        return fallback.substring(0, Math.min(fallback.length(), maxLength));
    }
}
