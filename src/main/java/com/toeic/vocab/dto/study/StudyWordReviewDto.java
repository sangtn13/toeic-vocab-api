package com.toeic.vocab.dto.study;

import java.util.UUID;

public record StudyWordReviewDto(
        UUID vocabularyId,
        String word,
        String meaning) {
}