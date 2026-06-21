package com.toeic.vocab.dto.study;

import java.util.UUID;

public record StudyUnitActionDto(
        UUID unitId,
        String title,
        Integer unitOrder) {
}
