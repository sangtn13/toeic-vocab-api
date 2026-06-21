package com.toeic.vocab.dto.admin;

import com.toeic.vocab.enums.StudySetStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminStudySetDto(
        UUID id,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        Integer displayOrder,
        StudySetStatus status,
        Integer unitCount,
        Integer vocabularyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
