package com.toeic.vocab.request.admin;

import com.toeic.vocab.enums.PartOfSpeech;
import com.toeic.vocab.enums.VocabularyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertVocabularyRequest(
    @NotBlank(message = "Word is required")
    @Size(max = 150, message = "Word must be at most 150 characters")
    String word,

    @NotBlank(message = "Meaning is required")
    @Size(max = 255, message = "Meaning must be at most 255 characters")
    String meaning,

    @Size(max = 1000, message = "Definition must be at most 1000 characters")
    String definition,

    @Size(max = 1000, message = "Example sentence must be at most 1000 characters")
    String exampleSentence,

    @Size(max = 1000, message = "Example translation must be at most 1000 characters")
    String exampleTranslation,

    @Size(max = 100, message = "US phonetic must be at most 100 characters")
    String phoneticUs,

    @Size(max = 100, message = "UK phonetic must be at most 100 characters")
    String phoneticUk,

    @Size(max = 500, message = "US pronunciation URL must be at most 500 characters")
    String pronunciationUsUrl,

    @Size(max = 500, message = "UK pronunciation URL must be at most 500 characters")
    String pronunciationUkUrl,

    @Size(max = 255, message = "Hint must be at most 255 characters")
    String hint,

    @NotNull(message = "Part of speech is required")
    PartOfSpeech partOfSpeech,

    @NotNull(message = "Difficulty level is required")
    VocabularyLevel difficultyLevel,

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder,

    @NotNull(message = "Active flag is required")
    Boolean active
) {
}
