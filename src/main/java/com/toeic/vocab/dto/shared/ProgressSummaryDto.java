package com.toeic.vocab.dto.shared;

public record ProgressSummaryDto(
        int totalWords,
        int learnedWords,
        int masteredWords,
        int percentage) {
}
