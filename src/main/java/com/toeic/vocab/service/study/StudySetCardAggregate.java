package com.toeic.vocab.service.study;

import com.toeic.vocab.enums.StudySetStatus;
import java.util.UUID;

public record StudySetCardAggregate(
        UUID id,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        StudySetStatus status,
        long totalUnits,
        long totalWords,
        long learnedWords,
        long masteredWords) {
}
