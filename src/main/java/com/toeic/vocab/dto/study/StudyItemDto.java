package com.toeic.vocab.dto.study;

import com.toeic.vocab.enums.PartOfSpeech;
import java.util.List;
import java.util.UUID;

public record StudyItemDto(
        UUID vocabularyId,
        Boolean mastered,
        String word,
        String meaning,
        String definition,
        String exampleSentence,
        String exampleSentenceMasked,
        String exampleTranslation,
        String phoneticUs,
        String phoneticUk,
        String pronunciationUsUrl,
        String pronunciationUkUrl,
        String hint,
        PartOfSpeech partOfSpeech,
        List<StudyChoiceDto> choices) {
}