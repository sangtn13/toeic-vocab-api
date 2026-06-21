package com.toeic.vocab.dto.study;

import java.util.UUID;

public record StudyUnitProgressDto(
        UUID id,
        String title,
        Integer unitOrder,
        Integer totalWords,
        Integer learnedWords,
        Integer masteredWords,
        Integer percentage,
        String status) {
}
