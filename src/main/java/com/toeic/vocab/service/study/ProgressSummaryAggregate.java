package com.toeic.vocab.service.study;

public record ProgressSummaryAggregate(
        long totalWords,
        long learnedWords,
        long masteredWords) {
}
