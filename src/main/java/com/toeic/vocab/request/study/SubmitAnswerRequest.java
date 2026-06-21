package com.toeic.vocab.request.study;

import com.toeic.vocab.enums.PracticeMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmitAnswerRequest(
    @NotNull(message = "Vocabulary ID is required")
    UUID vocabularyId,

    @NotNull(message = "Practice mode is required")
    PracticeMode practiceMode,

    @NotBlank(message = "Answer is required")
    String answer
) {
}
