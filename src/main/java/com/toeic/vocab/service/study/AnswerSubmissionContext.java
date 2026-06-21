package com.toeic.vocab.service.study;

import java.util.UUID;

public record AnswerSubmissionContext(
        UUID vocabularyId,
        String word,
        String meaning,
        UUID unitId,
        Integer unitOrder,
        UUID studySetId) {
}
