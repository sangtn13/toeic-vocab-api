package com.toeic.vocab.service.study;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record StudyProgressState(
        String progressToken,
        boolean persisted,
        UUID userId,
        String displayName,
        String clientKey,
        LocalDateTime createdAt,
        LocalDateTime lastStudiedAt,
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
}
