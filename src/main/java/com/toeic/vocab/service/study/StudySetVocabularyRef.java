package com.toeic.vocab.service.study;

import java.util.UUID;

public record StudySetVocabularyRef(
        UUID studySetId,
        UUID vocabularyId) {
}
