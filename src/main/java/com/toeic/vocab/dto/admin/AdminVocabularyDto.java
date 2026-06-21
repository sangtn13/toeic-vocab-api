package com.toeic.vocab.dto.admin;

import com.toeic.vocab.enums.PartOfSpeech;
import com.toeic.vocab.enums.VocabularyLevel;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminVocabularyDto(
        UUID id,
        UUID studySetId,
        String studySetTitle,
        UUID unitId,
        String unitTitle,
        String word,
        String meaning,
        String definition,
        String exampleSentence,
        String exampleTranslation,
        String phoneticUs,
        String phoneticUk,
        String pronunciationUsUrl,
        String pronunciationUkUrl,
        String hint,
        PartOfSpeech partOfSpeech,
        VocabularyLevel difficultyLevel,
        Integer displayOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
