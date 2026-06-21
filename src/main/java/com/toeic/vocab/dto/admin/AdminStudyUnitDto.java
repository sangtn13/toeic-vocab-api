package com.toeic.vocab.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminStudyUnitDto(
        UUID id,
        UUID studySetId,
        String studySetTitle,
        String title,
        String description,
        Integer unitOrder,
        Boolean active,
        Integer vocabularyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
