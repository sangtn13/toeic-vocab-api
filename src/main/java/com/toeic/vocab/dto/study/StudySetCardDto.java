package com.toeic.vocab.dto.study;

import com.toeic.vocab.enums.StudySetLearningStatus;
import java.util.UUID;

public record StudySetCardDto(
        UUID id,
        String title,
        String slug,
        String description,
        StudySetLearningStatus learningStatus,
        Integer totalUnits,
        Integer totalWords) {
}