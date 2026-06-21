package com.toeic.vocab.dto.study;

public record StudyProgressContextDto(
        String progressToken,
        String displayName,
        boolean persistent) {
}