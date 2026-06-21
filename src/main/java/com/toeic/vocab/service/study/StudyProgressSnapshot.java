package com.toeic.vocab.service.study;

public record StudyProgressSnapshot(
        int attemptCount,
        int correctCount,
        boolean mastered) {
}
