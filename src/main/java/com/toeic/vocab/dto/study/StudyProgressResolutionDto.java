package com.toeic.vocab.dto.study;

public record StudyProgressResolutionDto(
        StudyProgressContextDto progress,
        boolean created) {
}
